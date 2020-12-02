(ns viiguis.guis.flight-booker
  (:require
    [reagent.core :as r])
  (:import
    (goog.date UtcDateTime)))

; Dates (and JS dates in particular) are a pain
; If this was a real project, I'd use a library like https://github.com/juxt/tick

(defn valid-date?
  [date]
  (and (instance? UtcDateTime date)
       (not (js/isNaN (.getTime date)))))

(defn date->iso-string
  [date]
  (when (valid-date? date)
    (subs (.toUTCRfc3339String date) 0 10)))

(defn- zero-out-hours
  [date]
  (doto date (.setHours 0 0 0 0)))

(defn parse-date
  "Parses string as date, returns a UTC date"
  [s]
  (UtcDateTime/fromIsoString s))

(defn can-book?
  [{:keys [mode start return]}]
  (and (valid-date? start)
       (or (= "one-way" mode)
           (and (valid-date? return)
                (< (zero-out-hours start)
                   (zero-out-hours return))))))

(defn booking-message
  [{:keys [mode start return]}]
  (str "You have booked a " mode " flight on " (date->iso-string start)
       (when (= "return" mode)
         (str " returning on " (date->iso-string return)))))

(defn panel
  []
  (let [now (UtcDateTime.)
        flight (r/atom {:mode "one-way"
                        :start now
                        :return now})]
    (fn []
      [:div {:style {:display :flex
                     :flex-direction :column
                     :max-width "32em"
                     :margin "0 auto"}}
       [:select {:on-change (fn [ev]
                              (swap! flight assoc :mode (.. ev -target -value)))
                 :style {:margin-bottom "1em"}}
        [:option {:value "one-way"} "one-way flight"]
        [:option {:value "return"} "return flight"]]
       ; could use input type="date" here, but that would make it impossible on some browsers to select an invalid date
       ; and trigger the background color change
       [:input {:on-change (fn [ev]
                             (swap! flight assoc :start (parse-date (.. ev -target -value))))
                :default-value (date->iso-string (:start @flight))
                :style {:margin-bottom "1em"
                        :background-color (when-not (valid-date? (:start @flight)) "red")}}]
       [:input {:on-change (fn [ev]
                             (swap! flight assoc :return (parse-date (.. ev -target -value))))
                :default-value (date->iso-string (:return @flight))
                :disabled (not= "return" (:mode @flight))
                :style {:margin-bottom "1em"
                        :background-color (when-not (valid-date? (:return @flight)) "red")}}]
       [:button
        {:disabled (not (can-book? @flight))
         :on-click #(js/alert (booking-message @flight))}
        "Book"]])))
