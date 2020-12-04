(ns viiguis.guis.temperature-converter-test
  (:require
    [cljs.test :refer [deftest is]]
    [viiguis.guis.temperature-converter :as gui]))

(deftest celsius->fahrenheit-test
  (is (= -58 (gui/celsius->fahrenheit -50)))
  (is (= 32 (gui/celsius->fahrenheit 0)))
  (is (= 72.5 (gui/celsius->fahrenheit 22.5)))
  (is (= 212 (gui/celsius->fahrenheit 100))))

(deftest fahrenheit->celsius-test
  (is (= -50 (gui/fahrenheit->celsius -58)))
  (is (< -17.78 (gui/fahrenheit->celsius 0) -17.77))
  (is (= 0 (gui/fahrenheit->celsius 32))))