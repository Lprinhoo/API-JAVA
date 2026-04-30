package com.example.apiproject.security;

import com.example.apiproject.model.Cliente;
import com.example.apiproject.model.OficinaUser;
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
        Optional<OficinaUser> oficinaUser = oficinaUserRepository.findByUsername(username);
        if (oficinaUser.isPresent()) {
            return new User(oficinaUser.get().getUsername(), 
                           oficinaUser.get().getPassword(), 
                           Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); // Papel genérico
        }

        // Se não encontrar, tenta encontrar na tabela de clientes
        Optional<Cliente> cliente = clienteRepository.findByEmail(username);
        if (cliente.isPresent()) {
            return new User(cliente.get().getEmail(), 
                           "GOOGLE_AUTH", // Senha placeholder para clientes Google
                           Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); // Papel genérico
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}
