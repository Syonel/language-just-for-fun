package de.bzembrodt.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Scope {
    private final Map<String, RuntimeVariable> variables = new HashMap<>();
    public final Optional<Scope> parentScope;
    public final Optional<Scope> callerScope;

    public Scope(Optional<Scope> parentScope, Optional<Scope> callerScope) {
        this.parentScope = parentScope;
        this.callerScope = callerScope;
    }

    public boolean hasVariable(String name, boolean checkParents) {
        if (variables.containsKey(name)) {
            return true;
        }
        if (checkParents && parentScope.isPresent()) {
            return parentScope.get().hasVariable(name, true);
        }
        return false;
    }

    public void setVariable(String name, RuntimeVariable value) {
        variables.put(name, value);
    }

    public RuntimeVariable getVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        assert parentScope.isPresent();
        return parentScope.get().getVariable(name);
    }
}
