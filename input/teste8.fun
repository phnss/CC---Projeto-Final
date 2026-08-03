fun absoluto(x) {
    var resultado = 0;

    if x < 0 {
        resultado = 0 - x;
    } else {
        resultado = x;
    }

    return resultado;
}

main {
    return absoluto(0 - 15);
}