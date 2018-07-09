(ns spec-tac-joe.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [spec-tac-joe.core :as sut]))

(stest/instrument [`sut/game-loop `sut/get-input!]
                  {:replace {`sut/get-input!
                             (let [state (atom 0)]
                               (fn [turns]
                                 (swap! state inc)))}})

;; (println (first (stest/check [;;`sut/split-by-player
;;                              `sut/play
;;                              ;; `sut/interleave*
;;                               ])))

(deftest game-loop-integration
  (testing "If positions 0-8 are input consecutively, O should win after placing in 6"
    (let [result (with-out-str (sut/game-loop))]
      (is (= "Game Over: O wins!\n"
            result)))))
