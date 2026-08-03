fun quadrado(x) {
    return x * x;
}

fun somaQuadrados(a, b) {
    return quadrado(a) + quadrado(b);
}

main {
    return somaQuadrados(3, 4);
}