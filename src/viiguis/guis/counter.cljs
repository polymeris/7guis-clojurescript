(ns viiguis.guis.counter
  (:require
    [reagent.core :as r]))

(defn panel
  []
  (let [counter (r/atom 0)]
    (fn []
      [:<>
       [:output
        (str @counter)]
       [:button
        {:on-click (fn [_ev] (swap! counter inc))}
        "Count"]])))
