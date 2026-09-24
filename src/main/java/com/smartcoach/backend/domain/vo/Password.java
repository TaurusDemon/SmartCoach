package com.smartcoach.backend.domain.vo;

public record Password(String value) {
     public Password{
         if(value == null || value.isEmpty()){
             throw new IllegalArgumentException("La contraseña no puede ser nula");
         }
         if(value.length()<8){
             throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
         }
     }
}
