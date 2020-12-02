(ns viiguis.guis.circle-drawer
  (:require
    [reagent.core :as r]))

(defn circle
  [{:keys [geometry on-click selected]}]
  (let [[x y r] geometry]
    [:circle {:cx x
              :cy y
              :r r
              :stroke "black"
              :stroke-width ".5"
              :fill-opacity "0.2"
              :fill (if selected "black" "transparent")
              :on-click (fn [ev]
                          (on-click)
                          (.stopPropagation ev))}]))

(defn context-menu
  "The menu that appears when you select a circle"
  [{:keys [geometry on-click]}]
  ; If we wanted the button to look/feel more consistent we could have added a regular HTML button as SVG foreignObject
  (let [[x y _] geometry
        on-click (fn [ev]
                   (on-click)
                   (.stopPropagation ev))]
    [:g {:fill-opacity "0.8"}
     [:rect
      {:x x
       :y y
       :width 100
       :height 20
       :stroke "black"
       :stroke-width ".5"
       :fill "#eee"
       :cursor :pointer
       :on-click on-click}]
     [:text
      {:x (+ x 5)
       :y (+ y 14)
       :font-size 13
       :text-length 90
       :font-family "sans"
       :cursor :pointer
       :on-click on-click}
      "Adjust radius..."]]))

(defn edit-dialog
  [{:keys [initial-value on-change on-cancel on-confirm]}]
  [:div {:style {:position :fixed
                 :top 0
                 :left 0
                 :width "100vw"
                 :height "100vh"
                 :z-index 99
                 :background-color "rgba(0, 0, 0, 0.1)"}}
   [:div {:style {:position :fixed
                  :top "50vh"
                  :left "calc(50vw - 100px)"
                  :width "200px"
                  :z-index 100
                  :background-color "white"
                  :border "1px solid #aaa"
                  :display :flex
                  :flex-direction :column}}
    [:label {:style {:padding "0.5em"}}
     "Adjust diameter of circle:"
     [:input {:type :range
              :default-value initial-value
              :on-change (fn [ev]
                           (on-change (js/Number (.. ev -target -value))))
              :min 5
              :max 100}]]
    [:div {:style {:display :flex
                   :flex-direction :row
                   :justify-content :space-between
                   :padding "0.5em"}}
     [:button {:on-click on-cancel} "Cancel"]
     [:button {:on-click on-confirm} "OK"]]]])

(defn conj-edit
  [val edit]
  (-> val
      (update :edit-stack conj edit)
      (assoc :redo-stack '())))

(defn undo
  [{:keys [edit-stack selected-circle] :as val}]
  (let [edit (first edit-stack)]
    (cond-> val
            (= selected-circle edit) (assoc :selected-circle nil)
            true (update :redo-stack conj edit)
            true (update :edit-stack pop))))

(defn can-undo?
  [{:keys [edit-stack]}]
  (seq edit-stack))

(defn redo
  [{:keys [redo-stack] :as val}]
  (-> val
      (update :edit-stack conj (first redo-stack))
      (update :redo-stack pop)))

(defn can-redo?
  [{:keys [redo-stack]}]
  (seq redo-stack))

(defn circle-geometry
  [{:keys [edit-stack current-edit]}]
  (->> (if current-edit
         (conj edit-stack current-edit)
         edit-stack)
       (group-by (fn [[x y _r]] [x y]))
       (map (fn [[_ edits]] (first edits)))))

(defn panel
  []
  (let [state (r/atom {:selected-circle nil
                       :current-edit nil
                       :edit-stack '()
                       :redo-stack '()})]
    (fn []
      [:div {:style {:display :flex
                     :flex-direction :column}}
       [:div {:style {:display :flex
                      :flex-direction :row
                      :justify-content :space-evenly
                      :margin-bottom "1em"}}
        [:button
         {:on-click #(swap! state undo)
          :disabled (not (can-undo? @state))}
         "Undo"]
        [:button
         {:on-click #(swap! state redo)
          :disabled (not (can-redo? @state))}
         "Redo"]]
       [:svg {:on-click (fn [ev]
                          ; This is a bit hackish, won't work if there are transformations on the svg or if it is
                          ; padded. A more solid implementation would be to apply the inverse of the svg's transform
                          (let [bb (.getBoundingClientRect (.-target ev))
                                x (- (.-clientX ev) (.-x bb) 1) ; subract 1 for the border
                                y (- (.-clientY ev) (.-y bb) 1)]
                            (swap! state conj-edit [x y 20])))
              :style {:width "100%"
                      :height "calc(100vh - 20em)"
                      :min-height "300px"
                      :border "1px solid #ccc"}}
        [:<>
         (let [[selected-x selected-y _r] (:selected-circle @state)]
           (for [geometry (circle-geometry @state)]
             ^{:key geometry}
             [circle {:geometry geometry
                      :on-click #(swap! state assoc :selected-circle geometry)
                      :selected (= (take 2 geometry) [selected-x selected-y])}]))
         (when-let [geometry (and (not (:current-edit @state))
                                  (:selected-circle @state))]
           [context-menu {:geometry geometry
                          :on-click #(swap! state assoc :current-edit geometry)}])]]
       (when-let [[x y initial-r] (:current-edit @state)]
         [edit-dialog {:initial-value initial-r
                       :on-change #(swap! state assoc :current-edit [x y %])
                       :on-cancel #(swap! state assoc :current-edit nil)
                       :on-confirm #(let [edit (:current-edit @state)]
                                      (swap! state
                                             (fn [s]
                                               (-> s
                                                   (conj-edit edit)
                                                   (assoc :current-edit nil)
                                                   (assoc :selected-circle edit)))))}])])))
