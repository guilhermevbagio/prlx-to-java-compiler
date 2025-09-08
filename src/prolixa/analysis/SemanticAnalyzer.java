package prolixa.analysis;

import java.util.*;
import prolixa.node.*;

public class SemanticAnalyzer extends DepthFirstAdapter {
    
	private enum VariableType {
		NUMBER,
		ANSWER,
		SYMBOL,
		ERRO
	}
	private enum VariableKind {
		ALTERABLE,
		UNALTERABLE,
		VECTOR,
		ERRO
	}
	
    private class VariableData {
        VariableType type;
        VariableKind kind;
        boolean initialized;
        int scopeLevel;
        List<Integer> dimensions;
        
        VariableData(VariableType type, VariableKind kind, boolean initialized, int scopeLevel) {
            this.type = type;
            this.kind = kind;
            this.initialized = initialized;
            this.scopeLevel = scopeLevel;
            this.dimensions = null;
        }
        
        //veotr
        VariableData(VariableType type, VariableKind kind, List<Integer> dimensions, int scopeLevel) {
            this.type = type;
            this.kind = kind;
            this.initialized = false;
            this.scopeLevel = scopeLevel;
            this.dimensions = dimensions;
        }
    }
    

	}

    private VariableType getTypeString(PTipo tipo) {
        if (tipo instanceof ANumberTipo) return VariableType.NUMBER;
        if (tipo instanceof AAnswerTipo) return VariableType.ANSWER;
        if (tipo instanceof ASymbolTipo) return VariableType.SYMBOL;
        return VariableType.ERRO;
    }
    private VariableType getValueType(PValor valor) {
        if (valor instanceof ANumValor)  return VariableType.NUMBER;
        if (valor instanceof ABoolValor) return VariableType.ANSWER;
        if (valor instanceof ASymbValor) return VariableType.SYMBOL;
        if (valor instanceof AStrValor)  return VariableType.SYMBOL;
        return VariableType.ERRO;
    }
    
    private Stack<Map<String, VariableData>> scopeStack = new Stack<>();
    private int currentScopeLevel = 0;
    
 
    private int loopDepth = 0;
    
    private List<String> errors = new ArrayList<>();
    
    public SemanticAnalyzer() {
        enterScope();
    }
    
    
    
    private void enterScope() {
    	System.out.println("entering scope");
        scopeStack.push(new HashMap<String, VariableData>());
        currentScopeLevel++;
    }
    
    private void exitScope() {
    	System.out.println("leaving scope");
        scopeStack.pop();
        currentScopeLevel--;
    }
    
    
    
    private void declarar(String name, VariableData info) {
        Map<String, VariableData> currentScope = scopeStack.peek();
        currentScope.put(name, info);
    }
    
    private VariableData getVariableScope(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, VariableData> scope = scopeStack.get(i);

            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }
    
    private boolean getIsVariableInScope(String name) {
        return scopeStack.peek().containsKey(name);
    }

    private VariableType getExpressionType(PExp exp) {
        if (exp instanceof AValExp) {
            AValExp valExp = (AValExp) exp;
            
            return getValueType(valExp.getValor());
        }
        
        if (exp instanceof AVarExp) {
        	
            AVarExp varExp = (AVarExp) exp;
            PVar var = varExp.getVar();
            
            if (var instanceof AIdVar) {
                AIdVar idVar = (AIdVar) var;
                
                String name = idVar.getIdentificador().getText();
                VariableData info = getVariableScope(name);
                
                return info != null ? info.type : VariableType.ERRO;
            }
        }
        
        if (exp instanceof AAddExp || exp instanceof ASubExp || exp instanceof AMulExp
        		|| exp instanceof ADivExp || exp instanceof AModExp) {
            return VariableType.NUMBER;
        }
        
        if (exp instanceof AGtExp || exp instanceof ALtExp || exp instanceof AGeExp
        		|| exp instanceof ALeExp || exp instanceof AEqExp || exp instanceof ANeExp) {
            return VariableType.ANSWER;
        }
        
        if (exp instanceof AAndExp || exp instanceof AOrExp || exp instanceof AXorExp) {
            return VariableType.ANSWER;
        }
        
        if (exp instanceof ANegExp) {
            return VariableType.NUMBER;
        }
        
        return VariableType.ERRO;
    }
    
    private void reportError(String message, Node node) {
        String error = "Semantic Error: " + message;
        errors.add(error);
        System.err.println(error);
    }
    
    
    @Override
    public void outAMutDeclaracao(AMutDeclaracao node) {
        String id = node.getIdentificador().getText();
        VariableType type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        
        if (getIsVariableInScope(id)) {
            reportError(id + "' already declared in scope", node);
            return;
        }
        
        
        VariableData info = new VariableData(
        		type, 
        		VariableKind.ALTERABLE,
        		false, 
        		currentScopeLevel
        	);
        
        declarar(id, info);
    }
    
    
    @Override
    public void outAUnaltDeclaracao(AUnaltDeclaracao node) {
        String id = node.getIdentificador().getText();
        VariableType type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        if (getIsVariableInScope(id)) {
        	
            reportError(id + "' already declared in scope", node);
            return;
        }
        
        VariableData info = new VariableData(type, VariableKind.UNALTERABLE, false, currentScopeLevel);
        declarar(id, info);
    }
    
    @Override
    public void outAUnaltInitDeclaracao(AUnaltInitDeclaracao node) {
        String id = node.getIdentificador().getText();
        VariableType type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        if (getIsVariableInScope(id)) {
            reportError("'" + id + "' already declared in scope", node);
            return;
        }
        
        VariableType valueType = getValueType(node.getValor());
        if (type != valueType) {
            reportError("Type mismatch:'" + type + "' vs '" + valueType + "'", node);
        }
        
        VariableData info = new VariableData(
        		type, 
        		VariableKind.UNALTERABLE, 
        		true, 
        		currentScopeLevel
    		);
        declarar(id, info);
    }
    
    @Override
    public void outAVecDeclaracao(AVecDeclaracao node) {
        String id = node.getIdentificador().getText();
        VariableType type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        if (getIsVariableInScope(id)) {
            reportError("'" + id + "' already declared in scope", node);
            return;
        }
        
        List<Integer> dimensions = new ArrayList<>();
        AMatrizTamanhos tamanhos = (AMatrizTamanhos) node.getTamanhos();
        
        for (TNumero num : tamanhos.getNumero()) {
        	
            try {
            	
                dimensions.add(Integer.parseInt(num.getText()));
                
            } catch (NumberFormatException e) {
                
            	reportError("Invalid dimension: " + num.getText(), node);
            
            }
        }
        
        VariableData info = new VariableData(type, VariableKind.VECTOR, dimensions, currentScopeLevel);
        declarar(id, info);
    }

    @Override
    public void inABlocoComando(ABlocoComando node) {
        enterScope();
    }
    
    @Override
    public void outABlocoComando(ABlocoComando node) {
        exitScope();
    }
    
    @Override
    public void outAAtrComando(AAtrComando node) {
        PVar var = node.getVar();
        String varName = null;
        
        varName = ((AIdVar) var).getIdentificador().getText();

        
        if (varName != null) {
        	
            VariableData info = getVariableScope(varName);
            
            if (info == null) {
                reportError("Undefined variable '" + varName + "'", node);
                return;
            }
            
            if (info.kind == VariableKind.UNALTERABLE && info.initialized) {
                reportError("Cant change unalterable variable '" + varName + "'", node);
                return;
            }
            
            
            
            VariableType expType = getExpressionType(node.getExp());
            if (info.type != expType) {
                reportError("Type mismatch: expected '" + info.type + 
                           "' but got '" + expType + "'", node);
            }
            
            info.initialized = true;
        }
    }
    
    @Override
    public void outAInitComando(AInitComando node) {
        String id = node.getIdentificador().getText();
        VariableData info = getVariableScope(id);
        
        if (info == null) {
            reportError("Undefined variable '" + id + "'", node);
            return;
        }
        
        if (info.kind != VariableKind.UNALTERABLE) {
            reportError("Can only initialize unalterable variables", node);
            return;
        }
        
        if (info.initialized) {
            reportError("Variable '" + id + "' already initialized", node);
            return;
        }
        
        
        
        VariableType expType = getExpressionType(node.getExp());
        if (info.type != expType && expType != VariableType.ERRO) {
            reportError("Type mismatch in initialization: expected '" + info.type + 
                       "' but got '" + expType + "'", node);
        }
        
        info.initialized = true;
    }
    
    @Override
    public void inAWhileComando(AWhileComando node) {
        loopDepth++;
        
        VariableType condType = getExpressionType(node.getExp());
        if (condType != VariableType.ANSWER) {
            reportError("While condition must be of type 'answer'", node);
        }
    }
    
    @Override
    public void outAWhileComando(AWhileComando node) {
        loopDepth--;
    }
    
    @Override
    public void inAForComando(AForComando node) {
        loopDepth++;
        enterScope();
        

        PVar var = node.getVar();
        if (var instanceof AIdVar) {
            String iterName = ((AIdVar) var).getIdentificador().getText();

            VariableData iterInfo = new VariableData(VariableType.NUMBER, VariableKind.ALTERABLE, true, currentScopeLevel);
            
            declarar(iterName, iterInfo);
        }
        
        VariableType fromType = getExpressionType(node.getA());
        VariableType toType = getExpressionType(node.getB());
        VariableType byType = getExpressionType(node.getC());
        
        if (!(fromType == VariableType.NUMBER) && toType != VariableType.NUMBER && byType != VariableType.NUMBER) {
            reportError("For loop expressions must be a number", node);
        }
    }
    
    @Override
    public void outAForComando(AForComando node) {
        exitScope();
        loopDepth--;
    }
    
    @Override
    public void outABreakComando(ABreakComando node) {
        if (loopDepth == 0) {
            reportError("'abandon' outside of loop", node);
        }
    }
    
    @Override
    public void outAContComando(AContComando node) {
        if (loopDepth == 0) {
            reportError("'go to next iteration' statement outside of loop", node);
        }
    }
    
    @Override
    public void outAIfThenElseComando(AIfThenElseComando node) {
        VariableType condType = getExpressionType(node.getExp());
        if (condType != VariableType.ANSWER) {
            reportError("If condition must be of type 'answer'", node);
        }
    }
    
    @Override
    public void outAIfThenComando(AIfThenComando node) {
    	VariableType condType = getExpressionType(node.getExp());
        if (condType != VariableType.ANSWER) {
            reportError("If condition must be of type 'answer'", node);
        }
    }
    
    @Override
    public void outACapComando(ACapComando node) {
        for (PVar var : node.getVar()) {
            if (var instanceof AIdVar) {
                String name = ((AIdVar) var).getIdentificador().getText();
                VariableData info = getVariableScope(name);

                if (info == null) {
                    reportError("Undefined variable '" + name + "' in capture", node);
                } else if (info.kind == VariableKind.UNALTERABLE) {
                    reportError("Cannot capture into unalterable variable '" + name + "'", node);
                }
                else {
                    info.initialized = true;
                }
            }
        }
    }
    
    @Override
    public void outAShowComando(AShowComando node) {
        for (PExp exp : node.getExp()) {
            getExpressionType(exp);
        }
    }

    
    @Override
    public void outAIdVar(AIdVar node) {
        String id = node.getIdentificador().getText();
        
        VariableData info = getVariableScope(id);
        
        
        if (info == null) {
            reportError("Undefined variable '" + id + "'", node);
            
        } else if (!info.initialized && info.kind == VariableKind.UNALTERABLE) {
            reportError("'" + id + "' used before initialization", node);
        }
    }
    
    private void checkBinaryArithmeticOp(PExp left, PExp right, Node node) {
    	
    	VariableType leftType = getExpressionType(left);
    	VariableType rightType = getExpressionType(right);
        
        if (leftType != VariableType.NUMBER || rightType != VariableType.NUMBER) {
            reportError("operation requires number operands", node);
        }
    }
    
    private void checkBinaryLogicalOp(PExp left, PExp right, Node node) {
    	VariableType leftType = getExpressionType(left);
    	VariableType rightType = getExpressionType(right);
        
        if (leftType != VariableType.ANSWER ||
            rightType != VariableType.ANSWER) {
            reportError("operation requires answer operands", node);
        }
    }
    
    @Override
    public void outAAddExp(AAddExp node) {
        checkBinaryArithmeticOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outASubExp(ASubExp node) {
        checkBinaryArithmeticOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAMulExp(AMulExp node) {
        checkBinaryArithmeticOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outADivExp(ADivExp node) {
        checkBinaryArithmeticOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAModExp(AModExp node) {
        checkBinaryArithmeticOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAAndExp(AAndExp node) {
        checkBinaryLogicalOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAOrExp(AOrExp node) {
        checkBinaryLogicalOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAXorExp(AXorExp node) {
        checkBinaryLogicalOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outANegExp(ANegExp node) {
    	VariableType type = getExpressionType(node.getExp());
		if (type != VariableType.NUMBER) {
            reportError("Negative requires number operand", node);
        }
    }
    
    private void checkComparisonOp(PExp left, PExp right, Node node) {
    	VariableType leftType = getExpressionType(left);
    	VariableType rightType = getExpressionType(right);
        
        if (rightType != leftType || leftType == VariableType.ERRO) {
            reportError("Invalid operands in compariosn", node);
        }
    }
    
    @Override
    public void outAGtExp(AGtExp node) {
        checkComparisonOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outALtExp(ALtExp node) {
        checkComparisonOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAGeExp(AGeExp node) {
        checkComparisonOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outALeExp(ALeExp node) {
        checkComparisonOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outAEqExp(AEqExp node) {
        checkComparisonOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outANeExp(ANeExp node) {
        checkComparisonOp(node.getEsq(), node.getDir(), node);
    }
    
    @Override
    public void outStart(Start node) {
        if (!errors.isEmpty()) {
            System.err.println("\n---------- Semantic Errors: " + errors.size() + " error(s) ----------");
            System.exit(1);
        } else {
            System.out.println("----------Semantic Analysis Completed---------");
        }
    }
}