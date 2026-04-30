package com.example.apiproject.service;

import com.example.apiproject.model.Oficina;
import com.example.apiproject.model.OficinaRegistrationStatus;
import com.example.apiproject.repository.OficinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OficinaServiceImpl implements OficinaService {

    @Autowired
    private OficinaRepository oficinaRepository;

    @Override
    public Map<String, String> initiateOficinaRegistration() {
        String registrationToken = UUID.randomUUID().toString();

        Oficina newOficina = new Oficina();
        newOficina.setNome("Oficina Pendente de Cadastro"); // Nome temporário
        newOficina.setRegistrationToken(registrationToken);
        newOficina.setRegistrationStatus(OficinaRegistrationStatus.PAID_PENDING_DETAILS);

        oficinaRepository.save(newOficina);

        Map<String, String> response = new HashMap<>();
        response.put("registrationToken", registrationToken);
        response.put("message", "Registro de oficina iniciado. Use o token para completar o cadastro.");
        return response;
    }
}
