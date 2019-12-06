# VM Agent

## Clojure in 15 Minutes

### Hello, Clojure!

```clojure
(println "Hello, world!")
```

- The same in Python,
    ```python
    print('Hello, world!')
    ```
- `(` comes before the function, not after.
- `(x y z)` => Call function `x` with two arguments, `y` and `z`.

### Arithmetic

```clojure
(+ 1 2 3 4 5)  ;; => 15
(- 10 4 3 2)   ;; => 1
(- 1)          ;; => -1
(* 1 2 3 4 5)  ;; => 120
(/ 6 3 2)      ;; => 1
(/ 10)         ;; => 1/10
(float (/ 10)) ;; => 0.1
```

- Arithmetic operators in Clojure support more than two operands.
- `(f (g x))` => Call function `g` with argument `x`. Then call function `f` with the result of the previous function as argument.

### Vectors & Sets

```clojure
[1 "two" 3.0]         ;; => A vector
(nth [1 "two" 3.0] 2) ;; => 3.0

#{1 "two" 3.0}                 ;; => A set
(contains? #{1 "two" 3.0} 2.0) ;; => false
(#{1 "two" 3.0} 2.0)           ;; => false
```

- Collections in Clojure are not *typed*. Much like Python.
- Functions that return `true/false` usually end in a `?`. However, this is not enforced.
- A set is also a function(!)

### Maps

```clojure
{:one 1 :two 2.0 :three "three"}        ;; A map
(:one {:one 1 :two 2.0 :three "three"}) ;; => 1
```

- Symbols starting with `:` are keywords. Think `enums` or named strings. Keywords are primarily used as map keys.
- Keywords are also functions(!)

### Symbols

```clojure
(def x 10)
```

- `x` is a symbol.
- `10` is a value.
- `def` *binds* a symbol to a value.

### Functions

```clojure
(fn [x] (* x x))     ;; A function without a name
((fn [x] (* x x)) 2) ;; => 4
```

- `((fn [x] (* x x)) 2)` => Call function `(fn [x] (* x x))` with the argument `2`.

### Give that Function a Name

```clojure
(def square (fn [x] (* x x))) ;; Bind the symbol "square" to a function
(square 2)                    ;; => 4
```

### Macros

```clojure
(defn square [x] (* x x))
```

- This gets re-written as `(def square (fn [x] (* x x)))` before evaluation.
- Think `#define` in `C/C++` but macros are much more powerful.

```clojure
(-> x        ;; The thread-first macro
    (square)
    (cube))
```

- This gets re-written as `(cube (square x))` before evaluation.
- The thread-first macro `(->)` threads the previous result as the *first* parameter of the next function.
- Similarly, the thead-last macro `(->>)` threads the previous result as the *last* parameter of the next function.

### The Essence of a Functional Language, [Pure Functions](https://en.wikipedia.org/wiki/Pure_function)

*Definition:* A function that has no side-effects. Calling the function any number of times with the same input value will give the same output value.

- In the above example, `square` is a pure function. No matter how many times you call it with the value 2, the output will be 4.
- A function that debits money from a bank account is not a pure function.
- Printing to the console *is* a side-effect. It changes the state of `stdout`. So is writing to a file.
- Clojure is built around pure functions.

#### Example

```clojure
(def a-vector [1 "two" 3.0])
(conj a-vector :four)        ;; [1 "two" 3.0 :four]
a-vector                     ;; [1 "two" 3.0]
```

- `conj`(oin) takes a vector, adds an entry at the end and returns *another* vector. The original vector is not modified.

```clojure
(def a-map {:one 1 :two 2 :three 3})
(assoc a-map :four 4)                ;; => {:one 1 :two 2 :three 3 :four 4}
a-map                                ;; => {:one 1 :two 2 :three 3}
```

- `assoc`(iate) takes a map, adds an entry and returns *another* map. The original map is not modified.

### Map, Filter & Reduce

```clojure
(defn root-mean-square
  [xs]                             ;; A collection in Clojure is usually named xs, ys etc.
  (let [n       (count xs)
        squares (map square xs)    ;; => (1 4 9)
        sum     (reduce + squares) ;; => 14
        mean    (/ sum n)]         ;; => 4.666666666666667
    (Math/sqrt mean))              ;; => 2.160246899469287

(root-mean-square [1 2 3])         ;; Results at each step shown above.
```

- `(Math/sqrt mean)` => Calls Java `Math.sqrt`. Clojure runs on the JVM and has access to the complete Java ecosystem.
- `reduce` reduces a collection to a single value.

```clojure
(filter odd? [1 2 3 4 5 6 7 8 9 10]) ;; => [1 3 5 7 9]
```

### If & When

```clojure
(if   true  "true" "false") ;; => "true"
(when false "true")         ;; => nil
```

- Use `if` when there is also an else part.
- Use `when` otherwise.
- `nil` is what `null` is in other languages or `None` in Python.

### Cond

```clojure
(cond
  (odd?  x) "x is odd"
  (even? x) "x is even")
```

- `cond` is what `switch/case` is in other languages.

### Local Bindings

```clojure
(let [x 10]
  (println "x is: " x)) ;; => x is: 10

(println "x is: " x)      ;; Unable to resolve symbol: x in this context
```

- Like `def` but the binding is only available within the form `(let ...)`.

### Loop & Recur

```clojure
(defn factorial
  [x]
  (loop [n   x                  ;; "n" is locally bound to "x"
         acc 1]
    (if (> n 1)
      (recur (dec n) (* acc n)) ; Re-run the "loop" with new arguments.
      acc)))                    ; The current stack is replaced with the
                                ; new stack, so no stack overflow.
```

- `(> n 1)` => `n > 1` in other languages.
- `(< 0 x 11)` => Check if `x` is between 0 and 11 (both exclusive).
- `(= x y z)` => Check that `x`, `y` and `z` are all equal.
- Most operators in Clojure allow more than two operands.

### Type when It's (Really) Needed (clojure.spec)

### Can we Now Understand Clojure Code in the Wild?
