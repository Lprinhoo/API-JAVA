package com.example.apiproject.model;

public enum OficinaRegistrationStatus {
    PENDING_PAYMENT,          // Pagamento ainda não confirmado
    PAID_PENDING_DETAILS,     // Pagamento confirmado, aguardando preenchimento dos detalhes da oficina
    ACTIVE,                   // Oficina totalmente registrada e ativa
    INACTIVE,                 // Oficina inativa (ex: por falta de pagamento, desativação manual)
    REJECTED                  // Registro rejeitado
}
