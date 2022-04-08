package com.endava.endabank.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    private String description;

    @Column(nullable = false)
    private float amount;

    @Column(length = 40)
    private String address;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_account_issuer_id", nullable = false)
    private BankAccount bankAccountIssuer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_account_receiver_id", nullable = false)
    private BankAccount bankAccountReceiver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_type_id", nullable = false)
    private TransactionType transactionType;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TransactionState> transactionStates = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Transaction that = (Transaction) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}