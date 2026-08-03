fun ajustar(valor, casas) {
    var resultado = valor << casas;

    resultado++;

    return resultado >> 1;
}

main {
    return ajustar(5, 3) + (64 >> 3) + (3 << 2 + 1) + 17 % 5;
}