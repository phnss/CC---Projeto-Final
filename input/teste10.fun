fun fib(n) {
    var resultado = 0;

    if n < 2 {
        resultado = 1;
    } else {
        resultado = fib(n - 1) + fib(n - 2);
    }

    return resultado;
}

main {
    return fib(6);
}