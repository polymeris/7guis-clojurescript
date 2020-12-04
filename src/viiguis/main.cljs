(ns viiguis.main
  (:require
    [reagent.dom]
    [reagent.core :as r]

    [viiguis.guis.counter]
    [viiguis.guis.temperature-converter]
    [viiguis.guis.flight-booker]
    [viiguis.guis.timer]
    [viiguis.guis.crud]
    [viiguis.guis.circle-drawer]
    [viiguis.guis.cells]))

(def panels
  {"Counter" viiguis.guis.counter/panel
   "Temperature Converter" viiguis.guis.temperature-converter/panel
   "Flight Booker" viiguis.guis.flight-booker/panel
   "Timer" viiguis.guis.timer/panel
   "CRUD" viiguis.guis.crud/panel
   "Circle Drawer" viiguis.guis.circle-drawer/panel
   "Cells" viiguis.guis.cells/panel})

(defonce selected-panel (r/atom (first (keys panels))))

(defn nav-link
  [attrs label]
  [:li [:a attrs label]])

(defn root-view
  []
  [:<>
   [:header
    [:nav
     [:div [:h1 "7GUIs"]
      [:ul
       [nav-link {:href "https://github.com/polymeris/7guis-clojurescript"} "Code"]]]
     [:ul
      (for [[title _] panels]
        ^{:key title}
        ; Would be nice to have path-based routing here
        [nav-link {:href "#"
                   :on-click #(reset! selected-panel title)}
         title])]]]
   [:main
    [(get panels @selected-panel)]]])

(defn ^:export render []
  (reagent.dom/render
    [root-view]
    (js/document.querySelector "#app")))

(defn ^:export init []
  (render))
