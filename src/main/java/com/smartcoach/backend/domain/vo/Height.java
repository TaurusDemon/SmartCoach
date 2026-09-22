package com.smartcoach.backend.domain.vo;

public record Height(int valueCm) {
    public Height{
        if(valueCm <= 0)
            throw  new  IllegalArgumentException("La altura no puede ser menor que 0");
    }
}
