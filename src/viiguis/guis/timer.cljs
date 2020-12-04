(ns viiguis.guis.timer
  (:require
    [reagent.core :as r]))

(defn panel
  []
  (let [duration (r/atom 7)
        elapsed (r/atom 0)
        ticker (r/atom nil)]
    ; we use a "Form-3" component (i.e. with explicit lifecycle methods) because we want to clean up the timer
    ; before unmount
    ; c.f. https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md#form-3-a-class-with-life-cycle-methods
    (r/create-class
      {:display-name
       "timer/panel"

       :component-did-mount
       (fn [_this]
         ; we could get a way with a 100ms interval, but if you happen to restart the timer just as the interval
         ; is about to expire, that would be a bit imprecise (not that there is much of a guarantee that this won't
         ; drift anyways over a long period of time).
         ; 25ms also happens to be roughly the average human reaction time, so it seems like an appropiate pick.
         (reset! ticker (js/setInterval #(swap! elapsed (fn [t] (min @duration (+ 0.025 t)))) 25)))

       :component-will-unmount
       (fn [_this]
         (js/clearInterval @ticker))

       :reagent-render
       (fn []
         [:div.form
          [:label.timer--input-label
           "Elapsed time:"
           [:progress.timer--input
            {:value @elapsed
             :max @duration}]
           [:output
            (str (.toFixed @elapsed 1) "s")]]
          [:label.timer--input-label
           "Duration:"
           [:input.timer--input
            {:type :range
             :default-value @duration
             :on-change (fn [ev]
                          (reset! duration (js/Number (.. ev -target -value))))
             :min 0
             :max 100}]
           [:output
            (str @duration "s")]]
          [:button {:on-click #(reset! elapsed 0)}
           "Reset"]])})))
