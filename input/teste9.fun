fun somatorio(limite) {
    var n = 1;
    var soma = 0;

    while n < limite {
        soma = soma + n;
        n = n + 1;
    }

    return soma;
}

main {
    return somatorio(10);
}