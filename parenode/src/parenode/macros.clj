(ns parenode.macros
  (:require [clojure.core.match :refer [match]]))

(defn scheme-body->cljs [body]
  (if (seq? body) 
    (map
     (fn [statement] `(scheme->cljs ~statement))
     body)
    body))


(defmacro scheme->cljs [exp]
  (match [exp]

         [(false :<< seq?)] exp
         
         [(['car alist] :seq)] `(first (scheme->cljs ~alist))
         
         [(['cdr alist] :seq)] `(rest (scheme->cljs  ~alist))

         [(['cons elt alist] :seq)] `(cons (scheme->cljs ~elt) (scheme->cljs ~alist))
         
         [(['lambda args body] :seq)] `(fn ~(into [] args) ~(scheme-body->cljs body))

         [(['letrec* bindings body] :seq)] `(let ~(into []
                                                    (mapcat
                                                     (fn [[target a_binding]]
                                                       [target `(scheme->cljs ~a_binding)])
                                                     bindings))
                                              ~(scheme-body->cljs body))
         
         [(['define target a_binding] :seq)] `(def ~target (scheme->cljs ~a_binding)) 

         [(['quote an_exp] :seq)] `'~an_exp

         [([proc & args] :seq)] (cons `(scheme->cljs ~proc)  (scheme-body->cljs args))

         [([] :seq)] nil

         [:default] :error))
