(define (make-entity) 
  (cons '() (make-strong-eq-hash-table 100)))

(define (set-literal! entity lit) 
  (set-car! entity lit))  
