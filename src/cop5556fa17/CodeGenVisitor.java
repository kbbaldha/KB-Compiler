package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		if(declaration_Variable.agType == Type.INTEGER){
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		}
		else{
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, CodeGenUtils.getJVMType(Type.BOOLEAN), null, new Boolean(false));
		}
		
		if(declaration_Variable.e != null){
			declaration_Variable.e.visit(this, null);		
			
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, CodeGenUtils.getJVMType(declaration_Variable.e.agType));
			
		}
		//throw new UnsupportedOperationException();
		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
		
		expression_Binary.e0.visit(this, null);

		expression_Binary.e1.visit(this, null);
		
		Label startLabel = new Label();
		Label endLabel = new Label();
		
		switch(expression_Binary.op){
			case OP_PLUS:
				mv.visitInsn(IADD); break;
			case OP_MINUS:
				mv.visitInsn(ISUB); break;
			case OP_TIMES:
				mv.visitInsn(IMUL); break;
			case OP_DIV:
				mv.visitInsn(IDIV); break;
			case OP_AND:
				mv.visitInsn(IAND); break;
			case OP_OR:
				mv.visitInsn(IOR); break;
			case OP_MOD:
				mv.visitInsn(IREM); break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPLT, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
		}
		
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.agType);
		return expression_Binary;
		//throw new UnsupportedOperationException();
		
//		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
		
		switch(expression_Unary.e.agType){
		
		case INTEGER:{
			
			switch(expression_Unary.op){
			case OP_PLUS: 
				break;
			case OP_MINUS:
				mv.visitInsn(INEG);
				break;
			case OP_EXCL:
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
				break;
			default:
				break;
			}
		}
		case BOOLEAN:{
			
			Label falseLabel = new Label();
			Label endLabel = new Label();
			
			mv.visitJumpInsn(IFEQ, falseLabel);
			mv.visitLdcInsn(ICONST_0);//check for pop
			mv.visitJumpInsn(GOTO, endLabel);
			
			mv.visitLabel(falseLabel);
			mv.visitLdcInsn(ICONST_1);
			
			mv.visitLabel(endLabel);
			
		}
		default:
			break;
		}
		//throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.agType);
		return expression_Unary;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		expression_Conditional.visit(this, null);
		Label endLabel = new Label();
		Label falseLabel = new Label();
		
		mv.visitJumpInsn(IFEQ, falseLabel);
		expression_Conditional.trueExpression.visit(this,null);
		mv.visitJumpInsn(GOTO,endLabel);
		
		mv.visitLabel(falseLabel);
		expression_Conditional.falseExpression.visit(this,null);
		mv.visitLabel(endLabel);
		
		//throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.agType);
		return expression_Conditional;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, null);
		mv.visitInsn(AALOAD);
		
		//throw new UnsupportedOperationException();
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		
		mv.visitLdcInsn(new Integer(expression_IntLit.value));
		//throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		statement_Out.sink.visit(this, null);
		Declaration dec = statement_Out.getDec();
		
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/Printstream;");
		mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, CodeGenUtils.getJVMType(dec.agType));
		CodeGenUtils.genLogTOS(GRADE, mv, dec.agType);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" +  CodeGenUtils.getJVMType(dec.agType) + ")V",false);
		
		return statement_Out;
		// TODO HW6 remaining cases
		//throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		
		statement_In.source.visit(this,null);
		
		switch(statement_In.getDec().agType){
			case INTEGER:{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			}
			case BOOLEAN:{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, CodeGenUtils.getJVMType(Type.BOOLEAN));
				break;
			}
				
		}
		return statement_In;
		//throw new UnsupportedOperationException();
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		
		if(lhs.agType == Type.INTEGER || lhs.agType == Type.BOOLEAN){
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, CodeGenUtils.getJVMType(lhs.agType));
		}
		
		return lhs;
		//throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		
		//throw new UnsupportedOperationException();
		if(expression_BooleanLit.value){
			mv.visitInsn(ICONST_1);
		}
		else{
			mv.visitInsn(ICONST_0);
		}
		CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
 		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, CodeGenUtils.getJVMType(expression_Ident.agType));
		
		//throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.agType);
		return expression_Ident;
	}

}
