package com.smartcoach.backend.domain.vo;

public record Weight(double value) {
    public Weight{
        if(value <=0 )
            throw new  IllegalArgumentException("El peso no puede ser menor que 0");
    }
}
