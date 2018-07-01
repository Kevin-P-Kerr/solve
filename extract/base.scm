(define (eval-body body)
  (if (null? body) #f
    (let ((first (car body))
          (rest (cdr body)))
      (let ((condition (car first))
            (post (cdr first)))
        (if (or (null? rest) (condition))
          (post)
          (eval-body body))))))

(define (is-Int? a)
  (number? a))

(define (is-Str? a)
  (string? a))

(define (is-Lambda? a)
  (lambda? a))

(define  (has-Prop? s propName)
  (if (not (pair? s)) #f
    (if (not (pair? (car s))) #f
      (let ((p (car (car s))))
        (if (null? s) #f
          (eq? s propName))))))

(define (get-member s memberName)
  (if (not (pair? s)) '()
    (if (not (pair? (cdr s))) '()
      (let ((children (car (cdr s)))
            (m (cdr (cdr s)))
            (let ((v (hash-table/get m '())))
              (if (not (null? v)) v
                (get-member-list children memberName))))))))

(define (get-member-list l name)
  (if (null? l) l
    (let ((head (car l))
          (rest (cdr l)))
      (let ((r (get-member head name)))
        (if (not (null? r)) r
          (get-member-list rest))))))
