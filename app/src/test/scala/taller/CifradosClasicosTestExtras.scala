package taller

import  org.scalatest.funsuite.AnyFunSuite
import  org.junit.runner.RunWith
import  org.scalatestplus.junit.JUnitRunner

// Casos de Pruebas para el punto 1 y 2

@RunWith(classOf[JUnitRunner])
class CifradosClasicosTestExtras extends  AnyFunSuite {

  val c = new CifradosClasicos()
  import c._

  // Punto 1: cesar --------------------------------

  test("cesar: xyz con 3 da abc (completa un giro al alfabeto)") {
    assert(cesar("xyz", 3) == "abc")
  }

  test("cesar: abc con -1 da zab (porque retrocede y da una vuelta al abecedario)") {
    assert(cesar("abc", -1) == "zab")
  }

  test("cesar: un desplazamiento de 26 deja el mensaje igual (abecedario inglés = 26 letras)") {
    assert(cesar("hola", 26) == "hola")
  }

  test("cesar: un desplazamiento negativo grande equivale a su módulo") {
    // -29 equivale a -3, que a su vez equivale a 23
    assert(cesar("hola", -29) == "elix")
    assert(cesar("hola", -29) == cesar("hola", 23))
  }
  test("cesar: prueba de desplazamientos grandes, positivo y negativo") {
    assert(cesar("abc", 1000) == "mno")   // 1000 mod 26 = 12
    assert(cesar("abc", -1000) == "opq")  // -1000 equivale a -12, es decir 14
  }

  test("cesar: ROT13 con texto mixto y su propio inverso") {
    assert(cesar("hello, world 2026!", 13) == "uryyb, jbeyq 2026!")
    assert(cesar(cesar("hello, world 2026!", 13), 13) == "hello, world 2026!") //este es simple, simplemente se llamo 2
    //veces a la misma def cesar y una vez terminado el primer cesar, se completa el siguiente 13 + 13 = 26 == abecedario inglés

  }

  test("cesar: las letras que no son del abecedario inglés se mantienen igual") {
    assert(cesar("ñandú", 1) == "ñboeú")
  }

  test("cesar: dos cifrados seguidos suman sus desplazamientos") {
    val m = "programacion funcional"
    assert(cesar(cesar(m, 5), 9) == cesar(m, 14))
  }

  // Punto 2: cesarCola --------------------------------

  test("cesarCola: xyz con 3 da abc") {
    assert(cesarCola("xyz", 3) == "abc")
  }

  test("cesarCola: abc con -1 da zab") {
    assert(cesarCola("abc", -1) == "zab")
  }

  test("cesarCola: el mensaje vacío sale vacío") {
    assert(cesarCola("", 7) == "")
  }

  test("cesarCola: mayúsculas, espacios, comas y dígitos son ignorados y terminan igual") {
    assert(cesarCola("Hola, Mundo 2026", 5) == "Htqf, Mzsit 2026")
  }

  test("cesarCola: el acumulador inicial se conserva al principio del resultado") {
    assert(cesarCola("bc", 1, "xy") == "xycd")
  }

  test("cesarCola: descifrar con -k recupera el mensaje original, basicamente cifrado y descifrado") {
    val m = "cifrado de cola, version 2"
    assert(cesarCola(cesarCola(m, 17), -17) == m)
  }

  test("cesarCola: coincide con cesar para muchos desplazamientos") {
    val m = "el rapido zorro marron salta, 3 veces!"
    assert((-60 to 60).forall(k => cesarCola(m, k) == cesar(m, k)))
  }

  test("cesarCola: un mensaje largo de una sola letra") {
    val largo = "a" * 100000
    assert(cesarCola(largo, 1) == "b" * 100000)
  }

  // Punto 3: Frecuencias ---------------------

  test("frecuencias: mensaje con multiples empates y frecuencias variadas") {
    // Valida conteo complejo y orden de empates: 'a' y 'l' empatan con 4; 'd', 'i', 'u', 'v' empatan con 1
    assert(frecuencias("cali valle del cauca") == List(('a', 4), ('l', 4), ('c', 3), ('e', 2), ('d', 1), ('i', 1), ('u', 1), ('v', 1)))
  }

  test("frecuencias: solo Mayúsculas, número y símbolos devuelve lista vacía") {
    // Ningún carácter cumple esMinuscula
    assert(frecuencias("CALI 2026 #$%&/!") == List())
  }

  test("frecuencias: triple empate con frecuencias mayores a 1") {
    // Comprueba que entre 'x', 'y' y 'z' con 3 repeticiones se respete el orden alfabético
    assert(frecuencias("zzzyyyxxx") == List(('x', 3), ('y', 3), ('z', 3)))
  }

  test("frecuencias: Ignora caracteres fuera del alfabeto inglés (tildes y eñe)") {
    // 'ñ' y 'ó' se ignoran; solo deben contabilizarse 'a', 'c', 'n', 'o'
    val res = frecuencias("año cañón")
    assert(res == List(('a', 2), ('c', 1), ('n', 1), ('o', 1)))
  }

  test("frecuencias: distribución en escalera de frecuencias") {
    // 4 'd', 2 'b', 2 'c' y 1 'a'. Ante empate entre 'b' y 'c', 'b' continua a 'c'
    assert(frecuencias("ddddccbba") == List(('d', 4), ('b', 2), ('c', 2), ('a', 1)))
  }

  test("frecuencias: caracteres válidos separados por espacios y números") {
    // Verifica que los caracteres intermedios no afecten el flujo de la recursión
    val res = frecuencias("m 1 u 2 n 3 d 4 o")
    assert(res == List(('d', 1), ('m', 1), ('n', 1), ('o', 1), ('u', 1)))
  }

  test("frecuencias: alfabeto completo en orden inverso") {
    // Las 26 letras aparecen una vez; el resultado debe ser el abecedario ordenado de 'a' a 'z'
    val alfabetoAlreves = "zyxwvutsrqponmlkjihgfedcba"
    val esperado = ('a' to 'z').map(c => (c, 1)).toList
    assert(frecuencias(alfabetoAlreves) == esperado)
  }

  test("frecuencias: prueba de esfuerzo para verificar recursión de cola en memoria") {
    // 100.000 caracteres: si no fuera recursiva de cola, causaría StackOverflowError
    val textoLargo = ("a" * 50000) + ("z" * 50000)
    assert(frecuencias(textoLargo) == List(('a', 50000), ('z', 50000)))
  }

  // Punto 4: Desplazamiento Probable y Romper Cesar ---------------------

  test("desplazamientoProbable: texto donde la 'e' predomina devuelve desplazamiento 0") {
    val m = "este es el mejor ejemplo en espanol de un texto con muchas letras e"
    assert(desplazamientoProbable(m) == 0)
    assert(romperCesar(m) == m)
  }

  test("romperCesar: descifra correctamente con rotación estándar positiva (k = 4)") {
    val original = "este mensaje tiene que ser descifrado correctamente"
    val cifrado = cesarCola(original, 4)
    assert(desplazamientoProbable(cifrado) == 4)
    assert(romperCesar(cifrado) == original)
  }

  test("romperCesar: descifra con rotación que envuelve el abecedario (k = 25)") {
    // Desplazamiento 25 equivale a -1; 'e' se convierte en 'd'
    val original = "el elemento verde emerge entre el cesped"
    val cifrado = cesarCola(original, 25)
    assert(desplazamientoProbable(cifrado) == 25)
    assert(romperCesar(cifrado) == original)
  }

  test("romperCesar: descifra correctamente un mensaje cifrado con ROT13 (k = 13)") {
    val original = "este es un mensaje secreto en la noche estrellada"
    val cifrado = cesarCola(original, 13)
    assert(desplazamientoProbable(cifrado) == 13)
    assert(romperCesar(cifrado) == original)
  }

  test("desplazamientoProbable y romperCesar: texto sin letras minúsculas") {
    // Al no haber letras, el desplazamiento estimado debe ser 0 y la cadena no muta
    val m = "12345 !@#$ %^&*"
    assert(desplazamientoProbable(m) == 0)
    assert(romperCesar(m) == m)
  }

  test("desplazamientoProbable: empate múltiple sin la letra 'e'") {
    // 'b', 'c', 'd' aparecen 3 veces. Gana 'b'. Distancia modular de 'e' a 'b' es 23
    assert(desplazamientoProbable("bbbcccddd") == 23)
  }

  test("romperCesar: demuestra el fallo del método cuando la 'e' no es la más frecuente") {
    // En 'un perro', la 'r' es la más frecuente (2 veces). El algoritmo asume erróneamente que 'r' es 'e'
    val original = "un perro"
    val cifrado = cesarCola(original, 5)
    assert(romperCesar(cifrado) != original)
  }

  test("romperCesar: conserva signos, espacios y mayúsculas al descifrar") {
    val original = "El secreto es excelente: tres niveles (1, 2 y 3)!"
    val cifrado = cesarCola(original, 8)
    assert(desplazamientoProbable(cifrado) == 8)
    assert(romperCesar(cifrado) == original)
  }
  // Punto 5: Casos de prueba para el punto 5
  test("combinaciones: caso base n=0 devuelve 1") {
    val cifrado = new CifradosClasicos()
    assert(cifrado.combinaciones(0, 26) == BigInt(1))
  }

  test("combinaciones: alfabeto de 2 letras y longitud 2") {
    val cifrado = new CifradosClasicos()
    assert(cifrado.combinaciones(2, 2) == BigInt(2))
  }
  test("vigenere: cifrado basico con clave corta") {
    val cifrado = new CifradosClasicos()
    assert(cifrado.vigenere("ataque", "sol") == "shliip")
  }
  test("vigenere: los espacios y signos no consumen letras de la clave") {
    val cifrado = new CifradosClasicos()
    // 'l'+'a'='l', 'a'+'b'='b', ' ' sin cambio, 'c'+'a'='c', 'a'+'b'='b', 's'+'a'='s', 'a'+'b'='b'
    assert(cifrado.vigenere("la casa", "ab") == "lb cbsb")
  }

  test("vigenere: clave de una sola letra equivale a cifrado Cesar") {
    val cifrado = new CifradosClasicos()
    assert(cifrado.vigenere("scala", "c") == cifrado.cesar("scala", 2))
  }
}

