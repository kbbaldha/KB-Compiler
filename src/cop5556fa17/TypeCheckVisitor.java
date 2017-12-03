package cop5556fa17;

import java.net.URL;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
	HashMap<String,Declaration> symbolTable = new HashMap<String,Declaration>();

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		
	
	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		
		if(declaration_Variable.e!= null){
			declaration_Variable.e.visit(this, arg);
		}
		
		if(symbolTable.getOrDefault(declaration_Variable.name, null) != null){
			throwException(declaration_Variable.firstToken, "error in declaration var");
		}
		declaration_Variable.agType = TypeUtils.getType(declaration_Variable.type);
		symbolTable.put(declaration_Variable.name,declaration_Variable);
		
		if(declaration_Variable.e!= null){
			if(declaration_Variable.e.agType !=declaration_Variable.agType){
				throwException(declaration_Variable.firstToken, "expression typr not same in   declaration varianle");
			}
		}
		
		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		
		if(expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ){
			expression_Binary.agType = Type.BOOLEAN;
		}
		else if((expression_Binary.op == Kind.OP_GE || expression_Binary.op == Kind.OP_GT||expression_Binary.op == Kind.OP_LT 
				|| expression_Binary.op == Kind.OP_LE) && 
				expression_Binary.e0.agType == Type.INTEGER){
			expression_Binary.agType = Type.BOOLEAN;
		}
		else if((expression_Binary.op == Kind.OP_AND || expression_Binary.op == Kind.OP_OR) && 
				(expression_Binary.e0.agType == Type.INTEGER || expression_Binary.e0.agType == Type.BOOLEAN)){
			expression_Binary.agType = expression_Binary.e0.agType;
		}
		else if((expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS|| expression_Binary.op == Kind.OP_MOD||
				expression_Binary.op == Kind.OP_PLUS || expression_Binary.op == Kind.OP_POWER || expression_Binary.op == Kind.OP_TIMES)
				&& expression_Binary.e0.agType == Type.INTEGER){
			expression_Binary.agType = Type.INTEGER;
		}
		else{
			expression_Binary.agType = null;
		}
		
		if(expression_Binary.e0.agType == expression_Binary.e1.agType && expression_Binary.agType != null){
			
		}
		else{
			throwException(expression_Binary.firstToken, "binary  expr error");
		}
		return expression_Binary;
		
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		expression_Unary.e.visit(this, arg);
		
		if(expression_Unary.op == Kind.OP_EXCL && (expression_Unary.e.agType == Type.INTEGER || expression_Unary.e.agType == Type.BOOLEAN)){
			expression_Unary.agType = expression_Unary.e.agType;
		}
		else if((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS) && expression_Unary.e.agType == Type.INTEGER){
			expression_Unary.agType = Type.INTEGER;
		}
		else{
			expression_Unary.agType = null;
		}
		
		if(expression_Unary.agType == null){
			throwException(expression_Unary.firstToken, "UNARy exp error");
		}
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		index.e0.visit(this,arg);
		index.e1.visit(this,arg);   
		if(index.e0.agType == Type.INTEGER && index.e1.agType == Type.INTEGER){
			boolean isCartesian  = !(index.e0.firstToken.isKind(Kind.KW_r) && index.e1.firstToken.isKind(Kind.KW_a));
			index.setCartesian(isCartesian);
		}
		else{
			throwException(index.firstToken, "index token error");
		}
		return index;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		if(expression_PixelSelector.index != null)
			expression_PixelSelector.index.visit(this, arg);
		
		Type NameType = symbolTable.get(expression_PixelSelector.name) == null? null:symbolTable.get(expression_PixelSelector.name).agType;
		if(NameType == Type.IMAGE) {
			expression_PixelSelector.agType = Type.INTEGER;
		}
		else if(expression_PixelSelector.index == null){
			expression_PixelSelector.agType = NameType;
		}
		else{
			expression_PixelSelector.agType = null;
		}
		
		if(expression_PixelSelector.agType == null){
			throwException(expression_PixelSelector.firstToken, "pixel select error");
		}
		return expression_PixelSelector;
		
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		expression_Conditional.condition.visit(this, arg);
		expression_Conditional.trueExpression.visit(this, arg);
		expression_Conditional.falseExpression.visit(this, arg);
		
		expression_Conditional.agType = expression_Conditional.trueExpression.agType;
		
		if(expression_Conditional.condition.agType == Type.BOOLEAN && 
				expression_Conditional.trueExpression.agType == expression_Conditional.falseExpression.agType){
			
		}
		else{
			throwException(expression_Conditional.firstToken, "expression conditional");
		}
		return expression_Conditional;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {

		if(declaration_Image.source!= null){
			declaration_Image.source.visit(this, arg);
		}
		if(declaration_Image.xSize != null){
			declaration_Image.xSize.visit(this, arg);
			
		}
		if(declaration_Image.ySize != null){
			declaration_Image.ySize.visit(this, arg);
			
		}
		if(declaration_Image.ySize != null && declaration_Image.xSize != null){
			if(declaration_Image.ySize.agType != Type.INTEGER || declaration_Image.xSize.agType != Type.INTEGER){
				throwException(declaration_Image.firstToken, "xsize error");
			}
		}
		
		if(symbolTable.getOrDefault(declaration_Image.name, null) != null){
			throwException(declaration_Image.firstToken, "error in declaration var");
		}
		declaration_Image.agType = Type.IMAGE;
		symbolTable.put(declaration_Image.name,declaration_Image);
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		boolean isValidUrl = false;
		
		if(isValidURL(source_StringLiteral.fileOrUrl)) isValidUrl = true;
		
		source_StringLiteral.agType = isValidUrl? TypeUtils.Type.URL:TypeUtils.Type.FILE;	//check valid url	
		
		return source_StringLiteral.agType;
	}

	boolean isValidURL(String str){
		boolean isValid = true;		
		try{
		      new URL(str);
		  }
		    
		  catch(Exception e){
			  isValid = false;
		  }
		return isValid;		
	}
	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		
		source_CommandLineParam.paramNum.visit(this, arg);
		source_CommandLineParam.agType = source_CommandLineParam.paramNum.agType;
		
		if(source_CommandLineParam.agType  != Type.INTEGER){
			throwException(source_CommandLineParam.firstToken, "cmd line param");
			
		}
		
		return source_CommandLineParam.agType;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		source_Ident.agType = symbolTable.get(source_Ident.name) == null? null : symbolTable.get(source_Ident.name).agType;
		if(source_Ident.agType == Type.FILE || source_Ident.agType == Type.URL){
			return source_Ident.agType;
		}
		throw new SemanticException(source_Ident.firstToken, "source ident");
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
 
		//if(declaration_SourceSink.source!= null){
			declaration_SourceSink.source.visit(this, arg);
			
		//}
		
		if(symbolTable.getOrDefault(declaration_SourceSink.name, null) != null){
			throwException(declaration_SourceSink.firstToken, "error in declaration source sink");
		}
		declaration_SourceSink.agType = TypeUtils.getType(declaration_SourceSink.firstToken);
		
		if(declaration_SourceSink.agType != declaration_SourceSink.source.agType){
			throwException(declaration_SourceSink.firstToken, "error in visit dec");
		}
		
		symbolTable.put(declaration_SourceSink.name,declaration_SourceSink);
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		
		expression_IntLit.agType = Type.INTEGER;
		return expression_IntLit.agType;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		
		
		expression_FunctionAppWithExprArg.agType = Type.INTEGER;
		if(expression_FunctionAppWithExprArg.arg.agType != Type.INTEGER){
			throwException(expression_FunctionAppWithExprArg.firstToken, "function app with expr arg");
		}
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		
		if(expression_FunctionAppWithIndexArg.arg != null){
			expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		}
		
		expression_FunctionAppWithIndexArg.agType = Type.INTEGER;
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		return expression_FunctionAppWithIndexArg.agType;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		
		expression_PredefinedName.agType = Type.INTEGER;
		return expression_PredefinedName.agType;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statement_Out.sink.visit(this, arg);
		Declaration nameDeclaration = symbolTable.get(statement_Out.name);
		if(nameDeclaration == null){
			throwException(statement_Out.firstToken, "error in stmt out");
		}
		
		statement_Out.setDec(nameDeclaration);
		Type nameType = nameDeclaration.agType;
		//Sink sink = statement_Out. .sink;
		
		
		if(((nameType == Type.INTEGER || nameType == Type.BOOLEAN) && statement_Out.sink.agType == Type.SCREEN) || 
			(nameType == Type.IMAGE && (statement_Out.sink.agType == Type.SCREEN || statement_Out.sink.agType == Type.FILE))){
			
		}
		else{
			throwException(statement_Out.firstToken, "error in stmt out");
		}
		return statement_Out;
		//statement_Out.setDec(statement_Out.sink);
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		statement_In.source.visit(this, arg);
		Declaration nameDec = symbolTable.get(statement_In.name);
		statement_In.setDec(nameDec);
		
		if(nameDec!=null && nameDec.agType == statement_In.source.agType){
			
		}
		else{
			throwException(statement_In.firstToken, "stmt in error");
		}
		
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		statement_Assign.lhs.visit(this, arg);
		statement_Assign.e.visit(this, arg);
		
		
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);
		
		if(statement_Assign.lhs.agType != statement_Assign.e.agType){
			throwException(statement_Assign.firstToken, "error in  stmt assign");
		}
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(lhs.index !=null){
			lhs.index.visit(this, arg);
			lhs.isCartesian = lhs.index.isCartesian();
		}
		Declaration dec =  symbolTable.get(lhs.name);
		lhs.Declaration = dec; // check dec
		lhs.agType = (Type) (dec==null? null: dec.agType);
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		sink_SCREEN.agType = Type.SCREEN;
		
		return sink_SCREEN.agType;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_Ident.agType = symbolTable.get(sink_Ident.name).agType;
		if(sink_Ident.agType != Type.FILE){
			throwException(sink_Ident.firstToken, "sink ident");
		}
		return sink_Ident.agType;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		
		expression_BooleanLit.agType = Type.BOOLEAN;
		return expression_BooleanLit.agType;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(symbolTable.get(expression_Ident.name) == null){
			throwException(expression_Ident.firstToken, "expression ident error");
		}
		 expression_Ident.agType = symbolTable.get(expression_Ident.name).agType;
		 return expression_Ident.agType;
	}

	public void throwException(Token token, String msg) throws SemanticException{
		throw new SemanticException(token, msg);
	}
}

// sink ::== sink_ident | Sink_SCREEN

// check expression_functionapp