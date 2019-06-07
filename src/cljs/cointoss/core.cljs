(ns cointoss.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/about" :about]
    ["/counting" :counting]
    ["/coinflip" :coinflip]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

(path-for :about)
;; -------------------------
;; Page components

(defn home-page []
  (fn []
    [:span.main
     [:h2 "Welcome to Kevin's Current List"]
     [:ul
     	[:li [:a {:href (path-for :counting)} "Counting"]]
      [:li [:a {:href (path-for :coinflip)} "Coin Flip"]]
      ]]))



(defn about-page []
  (fn [] [:span.main
          [:h3 "A few projects that I tested with using Reagent and Clojure"]]))
(defonce app-state (atom {:text "Heads or Tails" :fill-number 0}))


(defn counting-page []
  (fn [] [:span.main
          [:h1 "Number" ]
          [:h2 (:fill-number @app-state)]
          [:button {:on-click (fn count-click [e]
          	(swap! app-state update-in [:fill-number] inc))}
          (str "Increase")]
          [:button {:on-click (fn count-click [e]
          	(swap! app-state update-in [:fill-number] dec))}
          (str "Decrease")]

          ]))




(defn coin-page []
  (fn [] [:span.main
          [:h1 (:text @app-state)]
          [:button {:on-click (fn reset-click [e]
            (let [flip (rand-int 2)]
    											(if (= flip 0) 
    													
                  (swap! app-state assoc-in [:text]
                                     (str "Heads")) 
    														
                  (swap! app-state assoc-in [:text]
                                 (str "Tails")))
    											))}
          (str "Flip Coin")]]))





;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page

    :counting #'counting-page
    :coinflip #'coin-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :about)} "About"]]]
       [page]
       [:footer
        [:p "These projects were generated with the help of "
         [:a {:href "https://github.com/reagent-project/reagent-template"} "Reagent Template"] " | "
         [:a {:href "https://opengisgal.wordpress.com/2018/05/12/clojurescript-reagent-tutorial-click-the-circle/"} "Click the Circle"]]]])))



;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
