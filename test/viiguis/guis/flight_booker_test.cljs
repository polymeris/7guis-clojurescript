(ns viiguis.guis.flight-booker-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [viiguis.guis.flight-booker :as gui])
  (:import
    (goog.date UtcDateTime)))

(deftest valid-date?-test
  (testing "invalid dates"
    (is (false? (gui/valid-date? nil)))
    (is (false? (gui/valid-date? (js/Date.)))))
  (testing "valid dates"
    (is (true? (gui/valid-date? (UtcDateTime.))))
    (is (true? (gui/valid-date? (UtcDateTime. 2020 1 1))))))

(deftest date->iso-string-test
  (is (nil? (gui/date->iso-string nil)))
  (is (nil? (gui/date->iso-string (js/Date. "invalid"))))
  (is (= "2020-12-02" (gui/date->iso-string (UtcDateTime. 2020 11 2)))))

(deftest parse-date-test
  (is (= "20201202T000000" (.toIsoString (gui/parse-date "2020-12-02")))))

(def today (UtcDateTime.))

(deftest can-book?-test
  (is (false? (gui/can-book? {:mode "one-way"})))
  (is (true? (gui/can-book? {:mode "one-way" :start today})))
  (is (false? (gui/can-book? {:mode "return" :start today :return today})))
  (is (false? (gui/can-book? {:mode "return" :start (UtcDateTime. 2020 1 1 0) :return (UtcDateTime. 2020 1 1 1)})))
  (is (true? (gui/can-book? {:mode "return" :start (UtcDateTime. 2020 1 1) :return (UtcDateTime. 2020 1 2)}))))

(deftest booking-message-test
  (is (= "You have booked a one-way flight on 1970-01-01"
         (gui/booking-message {:mode "one-way" :start  (UtcDateTime. 1970 0 1)})))
  (is (= "You have booked a return flight on 1970-01-01 returning on 1970-01-02"
         (gui/booking-message {:mode "return" :start (UtcDateTime. 1970 0 1) :return (UtcDateTime. 1970 0 2)}))))
