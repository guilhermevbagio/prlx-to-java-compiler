package prolixa.analysis;

import java.util.*;
import prolixa.node.*;

public class SemanticAnalyzer extends DepthFirstAdapter {
    
    private class VariableData {
        String type;        // "number", "answer", "symbol"
        String kind;        // "alterable", "unalterable", "vector"
        boolean initialized;
        int scopeLevel;
        List<Integer> dimensions; // para vetores
        
        VariableData(String type, String kind, boolean initialized, int scopeLevel) {
            this.type = type;
            this.kind = kind;
            this.initialized = initialized;
            this.scopeLevel = scopeLevel;
            this.dimensions = null;
        }
        
        //veotr
        VariableData(String type, String kind, List<Integer> dimensions, int scopeLevel) {
            this.type = type;
            this.kind = kind;
            this.initialized = false;
            this.scopeLevel = scopeLevel;
            this.dimensions = dimensions;
        }
    }
    
    private String getTypeString(PTipo tipo) {
        if (tipo instanceof ANumberTipo) return "number";
        if (tipo instanceof AAnswerTipo) return "answer";
        if (tipo instanceof ASymbolTipo) return "symbol";
        return "erro";
    }
    private String getValueType(PValor valor) {
        if (valor instanceof ANumValor) return "number";
        if (valor instanceof ABoolValor) return "answer";
        if (valor instanceof ASymbValor) return "symbol";
        if (valor instanceof AStrValor) return "symbol";
        return "erro";
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

    private String getExpressionType(PExp exp) {
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
                
                return info != null ? info.type : "erro";
            }
        }
        
        if (exp instanceof AAddExp || exp instanceof ASubExp || exp instanceof AMulExp
        		|| exp instanceof ADivExp || exp instanceof AModExp) {
            return "number";
        }
        
        if (exp instanceof AGtExp || exp instanceof ALtExp || exp instanceof AGeExp
        		|| exp instanceof ALeExp || exp instanceof AEqExp || exp instanceof ANeExp) {
            return "answer";
        }
        
        if (exp instanceof AAndExp || exp instanceof AOrExp || exp instanceof AXorExp) {
            return "answer";
        }
        
        if (exp instanceof ANegExp) {
            return "number";
        }
        
        return "erro";
    }
    
    private void reportError(String message, Node node) {
        String error = "Semantic Error: " + message;
        errors.add(error);
        System.err.println(error);
    }
    
    
    @Override
    public void outAMutDeclaracao(AMutDeclaracao node) {
        String id = node.getIdentificador().getText();
        String type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        
        if (getIsVariableInScope(id)) {
            reportError(id + "' already declared in scope", node);
            return;
        }
        
        
        VariableData info = new VariableData(
        		type, 
        		"alterable",
        		false, 
        		currentScopeLevel
        	);
        
        declarar(id, info);
    }
    
    
    @Override
    public void outAUnaltDeclaracao(AUnaltDeclaracao node) {
        String id = node.getIdentificador().getText();
        String type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        if (getIsVariableInScope(id)) {
        	
            reportError(id + "' already declared in scope", node);
            return;
        }
        
        VariableData info = new VariableData(type, "unalterable", false, currentScopeLevel);
        declarar(id, info);
    }
    
    @Override
    public void outAUnaltInitDeclaracao(AUnaltInitDeclaracao node) {
        String id = node.getIdentificador().getText();
        String type = getTypeString(node.getTipo());
        
        System.out.println("declaring: " + id);
        
        if (getIsVariableInScope(id)) {
            reportError("'" + id + "' already declared in scope", node);
            return;
        }
        
        String valueType = getValueType(node.getValor());
        if (!type.equals(valueType)) {
            reportError("Type mismatch:'" + type + "' vs '" + valueType + "'", node);
        }
        
        VariableData info = new VariableData(
        		type, 
        		"unalterable", 
        		true, 
        		currentScopeLevel
    		);
        declarar(id, info);
    }
    
    @Override
    public void outAVecDeclaracao(AVecDeclaracao node) {
        String id = node.getIdentificador().getText();
        String type = getTypeString(node.getTipo());
        
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
        
        VariableData info = new VariableData(type, "vector", dimensions, currentScopeLevel);
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
            
            if ("unalterable".equals(info.kind) && info.initialized) {
                reportError("Cant change unalterable variable '" + varName + "'", node);
                return;
            }
            
            
            
            String expType = getExpressionType(node.getExp());
            if (!info.type.equals(expType) && !"erro".equals(expType)) {
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
        
        if (!info.kind.equals("unalterable")) {
            reportError("Can only initialize unalterable variables", node);
            return;
        }
        
        if (info.initialized) {
            reportError("Variable '" + id + "' already initialized", node);
            return;
        }
        
        
        
        String expType = getExpressionType(node.getExp());
        if (!info.type.equals(expType) && !expType.equals("erro")) {
            reportError("Type mismatch in initialization: expected '" + info.type + 
                       "' but got '" + expType + "'", node);
        }
        
        info.initialized = true;
    }
    
    @Override
    public void inAWhileComando(AWhileComando node) {
        loopDepth++;
        
        String condType = getExpressionType(node.getExp());
        if (!condType.equals("answer")) {
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

            VariableData iterInfo = new VariableData("number", "alterable", true, currentScopeLevel);
            
            declarar(iterName, iterInfo);
        }
        
        String fromType = getExpressionType(node.getA());
        String toType = getExpressionType(node.getB());
        String byType = getExpressionType(node.getC());
        
        if (!"number".equals(fromType) && !"number".equals(toType) && !"number".equals(byType)) {
            reportError("For loop expression must be a number", node);
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
        String condType = getExpressionType(node.getExp());
        if (!"answer".equals(condType) && !"erro".equals(condType)) {
            reportError("If condition must be of type 'answer'", node);
        }
    }
    
    @Override
    public void outAIfThenComando(AIfThenComando node) {
        String condType = getExpressionType(node.getExp());
        if (!"answer".equals(condType) && !"erro".equals(condType)) {
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
                } else if ("unalterable".equals(info.kind)) {
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
            
        } else if (!info.initialized && "unalterable".equals(info.kind)) {
            reportError("Variable '" + id + "' used before initialization", node);
        }
    }
    
    private void checkBinaryArithmeticOp(PExp left, PExp right, Node node) {
    	
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);
        
        if ((!"number".equals(leftType)) || (!"number".equals(rightType))) {
            reportError("operation requires number operands", node);
        }
    }
    
    private void checkBinaryLogicalOp(PExp left, PExp right, Node node) {
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);
        
        if ((!"answer".equals(leftType) && !"erro".equals(leftType)) ||
            (!"answer".equals(rightType) && !"erro".equals(rightType))) {
            reportError("Logical operation requires answer (boolean) operands", node);
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
        String type = getExpressionType(node.getExp());
        if (!"number".equals(type) && !"erro".equals(type)) {
            reportError("Negation requires number operand", node);
        }
    }
    
    private void checkComparisonOp(PExp left, PExp right, Node node) {
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);
        
        if (!leftType.equals(rightType) || "erro".equals(leftType)) {
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