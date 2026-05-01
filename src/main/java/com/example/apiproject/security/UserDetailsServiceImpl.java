package com.example.apiproject.security;

import com.example.apiproject.model.Cliente;
import com.example.apiproject.model.OficinaUser;
import com.example.apiproject.model.OficinaRegistrationStatus;
import com.example.apiproject.repository.ClienteRepository;
import com.example.apiproject.repository.OficinaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private OficinaUserRepository oficinaUserRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tenta encontrar na tabela de usuários de oficina
        Optional<OficinaUser> oficinaUserOptional = oficinaUserRepository.findByUsername(username);
        if (oficinaUserOptional.isPresent()) {
            OficinaUser oficinaUser = oficinaUserOptional.get();
            // Verifica o status de registro da oficina associada
            if (oficinaUser.getOficina().getRegistrationStatus() == OficinaRegistrationStatus.ACTIVE) {
                // Retorna uma instância de OficinaUserDetails
                return OficinaUserDetails.build(oficinaUser);
            } else {
                // Se a oficina não estiver ativa, nega o login
                throw new UsernameNotFoundException("Oficina associada ao usuário não está ativa.");
            }
        }

        // Se não encontrar como OficinaUser, tenta encontrar na tabela de clientes
        Optional<Cliente> cliente = clienteRepository.findByEmail(username);
        if (cliente.isPresent()) {
            return new User(cliente.get().getEmail(),
                           "GOOGLE_AUTH", // Senha placeholder para clientes Google
                           Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))); // Atribui ROLE_CLIENTE
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}
