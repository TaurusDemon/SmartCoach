//package com.smartcoach.backend;
//
//import com.smartcoach.backend.aplication.usecase.RegisterUserCase;
//import com.smartcoach.backend.domain.model.User;
//import com.smartcoach.backend.domain.port.UserRepository;
//import com.smartcoach.backend.infrastructure.persistence.InMemoryUserRepository;
//
//public class Main {
//    public static void main(String[] args) {
//        UserRepository userRepository = new InMemoryUserRepository();
//
//        RegisterUserCase registerUserCase = new RegisterUserCase(userRepository);
//
//        try {
//            registerUserCase.execute("Carlos", "carletes@gmail.com");
//            registerUserCase.execute("Sofía", "emailinvalido");
//        }
//        catch (IllegalArgumentException e){
//            System.out.println("Expeción capturada: " + e.getMessage());
//        }
//
//
//    }
//}