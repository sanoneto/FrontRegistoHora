package com.desafio.service.Impl;

import com.desafio.service.desafios;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class desafiosImpl  implements desafios {
    @Override
    public String gerarHexagonal(String s, int numRows) {
        if (numRows <= 1 || s.length() <= numRows) {
            return s;
        }

        // Criamos uma lista de StringBuilders para cada linha
        List<StringBuilder> rows = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            rows.add(new StringBuilder());
        }
        System.out.println("StringBuilders rows: " + rows);
        int currentRow = 0;
        boolean goingDown = false;

        // Percorre a String original distribuindo os caracteres
        for (char c : s.toCharArray()) {
            rows.get(currentRow).append(c);
            System.out.println("String rows: " + rows);
            // Inverte a direção quando atinge o topo ou a base (efeito hexagonal)
            if (currentRow == 0 || currentRow == numRows - 1) {
                goingDown = !goingDown;
            }

            currentRow += goingDown ? 1 : -1;
        }

        // Concatenação final das 3 linhas num único resultado
        StringBuilder result = new StringBuilder();
        for (StringBuilder row : rows) {
            result.append(row);
        }

        return result.toString();
    }
}
