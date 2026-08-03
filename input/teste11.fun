fun fatorial(n) {
    var resultado = 1;

    if n > 1 {
        resultado = n * fatorial(n - 1);
    } else {
    }

    return resultado;
}

main {
    return fatorial(5);
}