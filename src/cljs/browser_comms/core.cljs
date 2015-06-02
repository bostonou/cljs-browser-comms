(ns cljs.browser-comms.core
  (:require [cljs.reader :as reader]
            [cljs-uuid-utils.core :as uuid]
            [cljs.core.async :as a]
            [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
            [clojure.string :as str]
            [schema.core :as t :include-macros true]
            [goog.events :as events]
            [goog.events.EventType :as EventType])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private uuid (uuid/uuid-string (uuid/make-random-uuid)))
(def ^:private next-id (atom 0))

(t/defn send! :- t/Bool
  "Broadcast data to all listening tabs/windows. Data should be serializable
  via pr-str & read-string."
  [data :- t/Any]
  ;;always add a changing and unique id so the value is different; the event
  ;; will only fire if the value has changed
  (.setItem js/localStorage ::e (pr-str {:data data
                                         :_id (str uuid (swap! next-id inc))}))
  true)

(defn- listener [c e]
  (let [event (.-event_ e)
        key (.-key event)
        payload (.-newValue event)]
    (when (and (not (str/blank? payload))
               (= (str ::e) key))
      (a/put! c (:data (reader/read-string payload))))))

(def ^:private event-key (atom nil))

(def Chan
  "core.async channel"
  ManyToManyChannel)

(t/defn listen! :- t/Bool
  "Listen for browser communication. Incoming data will be passed along the given
  channel. Will only allow one listener to be attached at any given time."
  [c :- Chan]
  (if-not @event-key
    (let [key (events/listen js/window EventType/STORAGE (partial listener c))]
      (reset! event-key key)
      true)
    false))

(t/defn unlisten! :- t/Bool
  "Remove listener for browser communication."
  []
  (if @event-key
    (do
      (events/unlistenByKey @event-key)
      (reset! event-key nil)
      true)
    false))

