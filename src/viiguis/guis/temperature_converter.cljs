(ns viiguis.guis.temperature-converter
  (:require
    [clojure.string :as string]))

(defn celsius->fahrenheit
  [c]
  (-> c (* 9) (/ 5) (+ 32)))

(defn fahrenheit->celsius
  [f]
  (-> f (- 32) (* 5) (/ 9)))

(defn format-output
  "Formats temperature to fixed 1 decimal"
  [t]
  (.toFixed t 1))

(defn- set-element-value!
  [selector value]
  (set! (.-value (js/document.querySelector selector))
        (format-output value)))

(defn- event-value-as-number
  [ev]
  (let [val (.. ev -target -value)]
    (when-not (or (string/blank? val) (js/isNaN val))
      val)))

(defn panel
  []
  ; In this simple case, leave the state in the inputs.
  ; This is basically the jQuery approach, not at all react-y.
  ; Not recommended, but possible, as this demonstrates.
  ; Anything even a bit more complex or where the value is used
  ; elsewhere would require a r/atom at the least
  [:div.form.flex-row
   [:label
    [:input#celsius.temperature-converter--input
     {:type :number
      :step 0.1
      :on-change (fn [ev]
                   (when-let [val (event-value-as-number ev)]
                     (->> (celsius->fahrenheit val)
                          (set-element-value! "input#fahrenheit"))))}]
    [:span "Celsius ="]]
   [:label
    [:input#fahrenheit.temperature-converter--input
     {:type :number
      :step 0.1
      :on-change (fn [ev]
                   (when-let [val (event-value-as-number ev)]
                     (->> (fahrenheit->celsius val)
                          (set-element-value! "input#celsius"))))}]
    [:span "Fahrenheit"]]])