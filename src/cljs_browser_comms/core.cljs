(ns cljs-browser-comms.core
  (:require [cljs.reader :as reader]
            [cljs.core.async :as a]
            [clojure.string :as str]
            [goog.events :as events]
            [goog.events.EventType :as EventType])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private id-prefix (random-uuid))
(def ^:private id-suffix (atom 0))

(defn send!
  "Broadcast data to all listening tabs/windows. Data should be serializable
  via pr-str & read-string."
  [data]
  ;;always add a changing and unique id so the value is different; the event
  ;; will only fire if the value has changed
  (.setItem js/localStorage ::e (pr-str {:data data
                                         :_id (str id-prefix (swap! id-suffix inc))}))
  true)

(defn- listener [c e]
  (let [event (.-event_ e)
        key (.-key event)
        payload (.-newValue event)]
    (when (and (not (str/blank? payload))
               (= (str ::e) key))
      (a/put! c (:data (reader/read-string payload))))))

(def ^:private event-key (atom nil))

(defn listen!
  "Listen for browser communication. Incoming data will be passed along the given
  channel. Will only allow one listener to be attached at any given time."
  [c]
  (if-not @event-key
    (let [key (events/listen js/window EventType/STORAGE (partial listener c))]
      (reset! event-key key)
      true)
    false))

(defn unlisten!
  "Remove listener for browser communication."
  []
  (if @event-key
    (do
      (events/unlistenByKey @event-key)
      (reset! event-key nil)
      true)
    false))

