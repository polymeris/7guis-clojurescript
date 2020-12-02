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
         [:div {:style {:display :flex
                        :flex-direction :column
                        :max-width "48em"
                        :margin "0 auto"}}
          [:label {:style {:display :flex
                           :flex-direction :row
                           :justify-content :space-between
                           :margin "1em"}}
           "Elapsed time:"
           [:progress {:value @elapsed
                       :max @duration
                       :style {:flex-grow 1
                               :margin "0 1em"}}]
           [:span {:style {:width "3em"}}
            (str (.toFixed @elapsed 1) "s")]]
          [:label {:style {:display :flex
                           :flex-direction :row
                           :justify-content :space-between
                           :margin "1em"}}
           "Duration:"
           [:input {:type :range
                    :default-value @duration
                    :on-change (fn [ev]
                                 (reset! duration (js/Number (.. ev -target -value))))
                    :min 0
                    :max 100
                    :style {:flex-grow 1
                            :margin "0 1em"}}]
           [:span {:style {:width "3em"}}
            (str @duration "s")]]
          [:button {:on-click #(reset! elapsed 0)}
           "Reset"]])})))
