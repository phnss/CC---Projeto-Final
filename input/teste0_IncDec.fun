fun ajustar(valor) {
    var resultado = valor;

    resultado++;
    resultado++;
    resultado--;

    return resultado;
}

var contador = 0;

main {
    while contador < 5 {
        contador++;
    }

    contador--;

    return ajustar(contador);
}