fun calcular(valor) {
    var resultado = valor;

    resultado += 6;
    resultado -= 2;
    resultado *= 3;
    resultado /= 2;
    resultado %= 8;
    resultado <<= 2;
    resultado >>= 1;

    return resultado;
}

main {
    return calcular(10);
}