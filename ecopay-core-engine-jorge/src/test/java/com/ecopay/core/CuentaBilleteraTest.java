package com.ecopay.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class CuentaBilleteraTest {

    // 1. Happy Path: Creación de cuenta válida
    @Test
    public void deberiaCrearCuentaConDatosValidos() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 500.0, 100);
        assertEquals(500.0, cuenta.getSaldo(), 0.001);
        assertEquals(100, cuenta.getPuntosLealtad());
    }


    // 2. Invariante: Saldo negativo
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaLanzarExcepcionAlCrearCuentaConSaldoNegativo() throws TransaccionInvalidaException {
        new CuentaBilletera("ACC-01", "Juan Perez", -50.0, 0);
    }

    // 3. Depósito válido
    @Test
    public void deberiaDepositarMontoValidoCorrectamente() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 500.0, 0);
        cuenta.depositar(250.0);
        assertEquals(750.0, cuenta.getSaldo(), 0.001);
    }

    // 4. Depósito inválido (negativo o cero)
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaLanzarExcepcionAlDepositarMontoCeroONegativo() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 500.0, 0);
        cuenta.depositar(-100.0);
    }

    // 5. Retiro válido
    @Test
    public void deberiaRetirarSaldoSuficienteCorrectamente() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 500.0, 0);
        cuenta.retirar(300.0);
        assertEquals(200.0, cuenta.getSaldo(), 0.001);
    }



    // 6. Sobregiro
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaLanzarExcepcionAlRetirarMontoMayorAlSaldo() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 500.0, 0);
        cuenta.retirar(600.0);
    }

    // 7. Compra sin bono VIP
    @Test
    public void deberiaAcumularPuntosBaseEnCompraSinBonoVIP() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 1000.0, 0);
        cuenta.realizarCompra(255.0);
        assertEquals(745.0, cuenta.getSaldo(), 0.001);
        assertEquals(25, cuenta.getPuntosLealtad());
    }

    // 8. Compra con bono VIP (>= 1000)
    @Test
    public void deberiaAplicarBonoVIPEnCompraIgualOMayorA1000() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 2000.0, 0);
        cuenta.realizarCompra(1000.0);
        assertEquals(1000.0, cuenta.getSaldo(), 0.001);
        assertEquals(150, cuenta.getPuntosLealtad());
    }

    // 9. Transferencia polimórfica exitosa
    @Test
    public void deberiaTransferirDineroPolimorficamenteEntreCuentas() throws TransaccionInvalidaException {
        CuentaBilletera origen = new CuentaBilletera("ACC-01", "Juan", 500.0, 0);
        CuentaBilletera destino = new CuentaBilletera("ACC-02", "Maria", 100.0, 0);
        origen.transferir(destino, 200.0);
        assertEquals(300.0, origen.getSaldo(), 0.001);
        assertEquals(300.0, destino.getSaldo(), 0.001);
    }

    // 10. Atomicidad en transferencia
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaGarantizarAtomicidadSiFallaLaTransferencia() throws TransaccionInvalidaException {
        CuentaBilletera origen = new CuentaBilletera("ACC-01", "Juan", 100.0, 0);
        CuentaBilletera destino = new CuentaBilletera("ACC-02", "Maria", 500.0, 0);
        try {
            origen.transferir(destino, 300.0);
        } finally {
            assertEquals(100.0, origen.getSaldo(), 0.001);
            assertEquals(500.0, destino.getSaldo(), 0.001);
        }
    }

    // 11. Canje de puntos válido
    @Test
    public void deberiaCanjearPuntosPorSaldoCorrectamente() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 100.0, 150);
        cuenta.canjearPuntosPorSaldo(100);
        assertEquals(110.0, cuenta.getSaldo(), 0.001);
        assertEquals(50, cuenta.getPuntosLealtad());
    }

    // 12. Canje no múltiplo de 10
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaLanzarExcepcionAlCanjearPuntosNoMultiplosDeDiez() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 100.0, 50);
        cuenta.canjearPuntosPorSaldo(25);
    }
    // 13. Test propio donde no se puede retirar mas saldo del disponible
    @Test(expected = TransaccionInvalidaException.class)
    public void soliocitaRetirarMasSaldoDelQuePosee() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 500.0, 0);
        cuenta.retirar(600.0);

    }
    //14. Test propio titular sin nombre (Se espera falla)
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaCrearCuentaConDatosIncorrectos() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01","", 500, 100);
        assertEquals(500.0, cuenta.getSaldo(), 0.001);
        assertEquals(100, cuenta.getPuntosLealtad());
    }
    //15. Test propio titular con nombre "null" (Se espera falla)
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaCrearCuentaSinNombre() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", null, 500, 100);
        assertEquals(500.0, cuenta.getSaldo(), 0.001);
        assertEquals(100, cuenta.getPuntosLealtad());
    }
    //16.
    @Test
    public void deberiaFallarBonoVIPPorFaltadeSaldo() throws TransaccionInvalidaException {
        CuentaBilletera cuenta = new CuentaBilletera("ACC-01", "Juan Perez", 2000.0, 0);
        cuenta.realizarCompra(1000.0);
        assertEquals(1000.0, cuenta.getSaldo(), 0.001);
        assertEquals(150, cuenta.getPuntosLealtad());
    }


}
