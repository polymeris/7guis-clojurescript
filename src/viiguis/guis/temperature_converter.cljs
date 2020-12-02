(ns viiguis.guis.temperature-converter)

(defn celsius->fahrenheit
  [c]
  (when c (-> c (* 9) (/ 5) (+ 32))))

(defn fahrenheit->celsius
  [f]
  (when f (-> f (- 32) (* 5) (/ 9))))

(defn format-output
  "Formats temperature to fixed 1 decimal"
  [t]
  (.toFixed t 1))

(defn- set-element-value!
  [selector value]
  (set! (.-value (js/document.querySelector selector))
        (format-output value)))

(defn panel
  []
  ; In this simple case, leave the state in the inputs.
  ; Anything even a bit more complex or where the value is used
  ; elsewhere would require a r/atom at the least
  [:<>
   [:input#celsius
    {:type :number
     :step 0.1
     :on-change (fn [ev]
                  (some->> (.. ev -target -value)
                           (js/Number)
                           (celsius->fahrenheit)
                           (set-element-value! "input#fahrenheit")))
     :style {:margin-right "1em"}}]
   [:span "Celsius ="]
   [:input#fahrenheit
    {:type :number
     :step 0.1
     :on-change (fn [ev]
                  (some->> (.. ev -target -value)
                           (js/Number)
                           (fahrenheit->celsius)
                           (set-element-value! "input#celsius")))
     :style {:margin "0 1em"}}]
   [:span "Fahrenheit"]])