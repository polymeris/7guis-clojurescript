(ns viiguis.guis.cells
  (:require
    [reagent.core :as r]
    [clojure.string :as string]
    [cljs.reader]
    [clojure.walk :as walk]))

(def operators
  {:sum (fn [& args] (apply + args))
   :sub (fn [& args] (apply - args))
   :div (fn [& args] (apply / args))
   :mul (fn [& args] (apply * args))
   :avg (fn [& args] (/ (apply + args) (count args)))})

(defn- ->operator
  [op]
  (when (symbol? op)
    (get operators (-> op name string/lower-case keyword))))

(def reference-regex
  #"([A-Z][0-9]{1,2})(:([A-Z][0-9]{1,2}))?")

(defn reference->key
  "Turns a reference string (like B6) into a [col row] pair"
  [ref]
  [(- (.charCodeAt (subs ref 0 1)) 65)
   (js/Number (subs ref 1))])

(defn resolve-ref [sheet & keys]
  (->> keys
       (map #(some-> (get sheet %) deref :value))
       (keep identity)
       (into [])))

(defn- ->reference
  [r]
  (when (symbol? r)
    (let [[_ from _ to] (re-matches reference-regex (name r))
          keys (if to
                 (let [[col-from row-from] (reference->key from)
                       [col-to row-to] (reference->key to)]
                   (for [col (range col-from (inc col-to))
                         row (range row-from (inc row-to))]
                     [col row]))
                 [(reference->key from)])]
      (cons :ref keys))))

(defn- ->literal
  [literal]
  (cond
    (js/isNaN literal) literal
    (= "" literal) nil
    :default (js/Number literal)))

(defn- ->formula
  [formula]
  (when (and (string? formula) (string/starts-with? formula "="))
    formula))

(defn- parse-formula*
  [formula]
  (->> (cljs.reader/read-string formula)
       (walk/postwalk
         (fn [x]
           (or (->operator x)
               (->reference x)
               (->literal x))))))

(def parse-formula
  (memoize (fn [formula]
             (if (->formula formula)
               (try
                 (parse-formula* (subs formula 1))
                 (catch js/Error ex
                   (ex-info (str ex) {:label "WAT?" :formula formula})))
               (->literal formula)))))

(defn eval-formula*
  [sheet formula]
  (walk/postwalk
    (fn [x]
      (if (seq? x)
        (if (= :ref (first x))
          (apply resolve-ref sheet (rest x))
          (apply (first x) (flatten (rest x))))
        x))
    formula))

(defn eval-formula
  [sheet formula]
  (try
    (let [val (eval-formula* sheet formula)]
      (if (coll? val)
        (first val)
        val))
    (catch js/Error ex
      (ex-info (str ex) {:label "ERR!" :formula formula}))))

(defn- make-cell
  [key]
  (r/atom {:key key
           :formula nil
           :value nil}))

(defn make-sheet
  [cols rows]
  (->> (for [c (range cols)
             r (range rows)]
         (make-cell [c r]))
       (map (juxt (comp :key deref) identity))
       (into {})))

(defn- formula-references*
  [formula]
  (let [cells (walk/postwalk (fn [x]
                               (if (seq? x)
                                 (cond (not (ifn? (first x))) x
                                       (= :ref (first x)) (rest x)
                                       :default (apply concat (filter seq? (rest x))))
                                 x))
                             (parse-formula formula))]
    (if (seq? cells)
      cells
      [])))

(def formula-references (memoize formula-references*))

(defn formula-cells
  [sheet formula]
  (map #(get sheet %) (formula-references formula)))

(defn- set-formula!
  [sheet key new-formula]
  (let [cell (get sheet key)
        old-formula (:formula @cell)]
    (js/console.log "Updating formula at" (str key) "from" old-formula "to" new-formula)
    (doseq [watched-cell (formula-cells sheet old-formula)]
      (remove-watch watched-cell key))
    (doseq [watched-cell (formula-cells sheet new-formula)]
      (add-watch watched-cell key
                 (fn [_k r old-val new-val]
                   (js/console.log "Update value of" (str key) "triggered by change in" (str (:key @r)))
                   (when (not= (:value old-val) (:value new-val))
                     ; make this async
                     (js/setTimeout
                       (fn []
                         (->> (parse-formula new-formula)
                              (eval-formula sheet)
                              (swap! cell assoc :value))))))))
    (let [new-value (->> (parse-formula new-formula)
                         (eval-formula sheet))]
      (swap! cell assoc
             :formula new-formula
             :value new-value))
    cell))

(defn set-cell!
  [sheet key value]
  (if (->formula value)
    (set-formula! sheet key value)
    (swap! (get sheet key)
           assoc :value (->literal value)
           :formula nil)))

(defonce cols 26)
(defonce rows 100)

(defonce sheet
  (make-sheet cols rows))

(defn table-cell
  [sheet key]
  (let [focused (r/atom false)
        cell (get sheet key)]
    (fn [sheet key]
      (let [value (:value @cell)
            formula (:formula @cell)
            error-label (:label (ex-data value))
            on-blur (fn [ev]
                      (reset! focused false)
                      (set! (.. ev -target -value) nil))]
        [:td.cells-table--cell
         [:input.cells-table--input
          {:placeholder (str (or error-label value))
           :on-focus (fn [ev]
                       (reset! focused true)
                       (set! (.. ev -target -value)
                             (or formula value)))
           :on-blur on-blur
           :on-key-press (fn [ev]
                           (when (= "Enter" (.-key ev))
                             (.preventDefault ev)
                             (set-cell! sheet key (.. ev -target -value))
                             (.blur (.-target ev))
                             (on-blur ev)))
           :class [(when @focused
                     "cells-table--input--focused")
                   (when formula
                     "cells-table--input--formula")
                   (when (and (string? value) (string/starts-with? value "#"))
                     "cells-table--input--bold")
                   (when error-label
                     "cells-table--input--error")]}]]))))

(defn table-row
  [sheet row-n]
  [:tr
   [:td.cells-table--row-header
    row-n]
   (->> (range cols)
        (map (fn [c]
               (let [key [c row-n]]
                 ^{:key key} [table-cell sheet key]))))])

(defn panel
  []
  [:<>
   [:div.cells--sheet-container
    [:table
     [:thead (->> (range (inc cols))
                  (map (fn [c] [:th (when (pos? c) (char (+ 64 c)))]))
                  (into [:tr]))]
     (->> (range rows)
          (map (fn [r] ^{:key r} [table-row sheet r]))
          (into [:tbody]))]]
   [:small
    [:p "Formulas start with = followed by s-expressions. Commit changes with Enter. Functions are variadic."]
    [:p "Available: " (string/join ", " (map name (keys operators)))]]])

;; add some demo data
(defonce demo
  (when true
    (set-cell! sheet [0 0] "#Operations")
    (set-cell! sheet [1 0] 1)
    (set-cell! sheet [1 1] 2.5)
    (set-cell! sheet [1 2] -10)
    (set-cell! sheet [0 3] "Sum of range")
    (set-cell! sheet [1 3] "=(SUM B0:B2)")
    ; overwrite previous value
    (set-cell! sheet [1 0] 30)
    (set-cell! sheet [0 4] "2nd level derived")
    (set-cell! sheet [1 4] "=(DIV B1 B3)")
    (set-cell! sheet [0 5] "Mix ranges & literals")
    (set-cell! sheet [1 5] "=(SUB B1 B3 15)")
    (set-cell! sheet [0 6] "Nested")
    (set-cell! sheet [1 6] "=(MUL (SUM B1 B2) B3 15)")
    (set-cell! sheet [0 8] "Avg ignores empty cells")
    (set-cell! sheet [1 8] "=(AVG B0 B2 B7)")
    (set-cell! sheet [0 9] "Repeated refs")
    (set-cell! sheet [1 9] "=(SUM B0 B0 B0)")
    (set-cell! sheet [2 0] "#Errors")
    (set-cell! sheet [2 1] "=(")
    (set-cell! sheet [2 2] "=B1000")
    (set-cell! sheet [2 3] "=(DIV foo)")))