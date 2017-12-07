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
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		declareStaticVariables();
		
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
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
	
	private void declareStaticVariables(){
		fv=cw.visitField(ACC_STATIC,"x", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		
		fv=cw.visitField(ACC_STATIC,"y", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		
		fv=cw.visitField(ACC_STATIC,"X", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		fv=cw.visitField(ACC_STATIC,"Y", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		fv=cw.visitField(ACC_STATIC,"r", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		fv=cw.visitField(ACC_STATIC,"a", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		fv=cw.visitField(ACC_STATIC,"R", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		fv=cw.visitField(ACC_STATIC,"A", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
		fv.visitEnd();
		
		fv=cw.visitField(ACC_STATIC,"DEF_X", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(256));
		fv.visitEnd();
		
		fv=cw.visitField(ACC_STATIC,"DEF_Y", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(256));
		fv.visitEnd();
		
		fv=cw.visitField(ACC_STATIC,"Z", CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(16777215));
		fv.visitEnd();
		
		/*X:  the upper bound on the value of loop index x.  It is also the width of the image. Obtain by invoking the ImageSupport.getX method
		Y:  the upper bound on the value of the loop index y.  It is also the height of the image.  Obtain by invoking the ImageSupport.getY method.
		r:  the radius in the polar representation of cartesian location x and y.  Obtain from x and y with RuntimeFunctions.polar_r.
		a:  the angle, in degrees, in the polar representation of cartesian location x and y.  Obtain from x and y with RuntimeFunctions.polar_a.
		R:  the upper bound on r, obtain from polar_r(X,Y)
		A:  the upper bound on a, obtain from polar_a(0,Y)
		The final three are fixed constants.  You can handle this by defining and initializing a variable, or just by letting visitExpression_PredefinedName load the constant value.
		DEF_X:  the default image width.  For simplicity, let this be 256.
		DEF_Y:  the default image height.  For simplicity, let this be 256.
		Z:  the value of a white pixel.  This is 0xFFFFFF or  16777215.*/


	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		if(declaration_Variable.agType == Type.INTEGER){
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, CodeGenUtils.getJVMType(Type.INTEGER), null, new Integer(0));
			fv.visitEnd();
		}
		else{
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, CodeGenUtils.getJVMType(Type.BOOLEAN), null, new Boolean(false));
			fv.visitEnd();
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
				mv.visitInsn(ISUB); 
				mv.visitLabel(endLabel);
				break;
				
			case OP_TIMES:
				mv.visitInsn(IMUL); break;
			case OP_DIV:
				mv.visitInsn(IDIV); break;
			case OP_AND:
				mv.visitInsn(IAND); break;
			case OP_OR:
				mv.visitInsn(IOR); break;
			case OP_MOD:
				mv.visitInsn(IREM); 
				//mv.visitJumpInsn(GOTO, endLabel);
				//mv.visitLabel(startLabel);
				//mv.visitLdcInsn(true);
				//mv.visitLabel(endLabel); 
				break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPLT, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel); 
				break;
		}
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.agType);
		return expression_Binary;
		//throw new UnsupportedOperationException();
		
//		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
		expression_Unary.e.visit(this, arg);
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
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.agType);
		return expression_Unary;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(!index.isCartesian())
		{
			//top 3| 1|2|3
		  //1|2|1|2
			//c_y|c_x
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_x", RuntimeFunctions.cart_xSig,false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_y", RuntimeFunctions.cart_ySig,false);
			
		}
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, CodeGenUtils.getJVMType(Type.IMAGE));
		//check
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getPixel", ImageSupport.getPixelSig,false);
		 
		return expression_PixelSelector;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		expression_Conditional.condition.visit(this, null);
		Label endLabel = new Label();
		Label falseLabel = new Label();
		
		mv.visitJumpInsn(IFEQ, falseLabel);
		expression_Conditional.trueExpression.visit(this,null);
		mv.visitJumpInsn(GOTO,endLabel);
		
		mv.visitLabel(falseLabel);
		expression_Conditional.falseExpression.visit(this,null);
		mv.visitLabel(endLabel);
		
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.agType);
		return expression_Conditional;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		if(declaration_Image.agType == Type.IMAGE)
		{
			fv=cw.visitField(ACC_STATIC,declaration_Image.name, ImageSupport.ImageDesc, null, null);
			fv.visitEnd();
			if(declaration_Image.source != null)
			{
				declaration_Image.source.visit(this, arg);
			
			
				if(declaration_Image.xSize != null && declaration_Image.ySize != null)
				{
					declaration_Image.xSize.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					declaration_Image.ySize.visit(this, arg);	
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			
				}
				else
				{
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}
			
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig,false);
			}
			else
			{
				if(declaration_Image.xSize != null && declaration_Image.ySize != null)
				{
					declaration_Image.xSize.visit(this, arg);
					declaration_Image.ySize.visit(this, arg);					
			
				}
				else
				{
					mv.visitFieldInsn(GETSTATIC,className,"DEF_X",CodeGenUtils.getJVMType(Type.INTEGER));
					mv.visitFieldInsn(GETSTATIC,className,"DEF_Y",CodeGenUtils.getJVMType(Type.INTEGER));					
					
				}
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"makeImage", ImageSupport.makeImageSig,false);
			}
		}
		mv.visitFieldInsn(PUTSTATIC,className,declaration_Image.name, CodeGenUtils.getJVMType(Type.IMAGE));
		return declaration_Image;
		//throw new UnsupportedOperationException();
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);	
		return source_StringLiteral;
		//throw new UnsupportedOperationException();
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

	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		//check
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, CodeGenUtils.getJVMType(Type.STRING));
		return source_Ident;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		//add field ro class
		fv = cw.visitField(ACC_STATIC,declaration_SourceSink.name, CodeGenUtils.getJVMType(Type.STRING), null, null);
		fv.visitEnd();
		
		if(declaration_SourceSink.source!=null)
		{
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC,className,declaration_SourceSink.name, CodeGenUtils.getJVMType(Type.STRING));			
		}
		//doubt :: 
		return declaration_SourceSink;
		//throw new UnsupportedOperationException();
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		
		mv.visitLdcInsn(new Integer(expression_IntLit.value));
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		
		switch (expression_FunctionAppWithExprArg.function)
		{
			case KW_abs:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"abs", RuntimeFunctions.absSig,false);
				break;
			case KW_log:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"log", RuntimeFunctions.logSig,false);
				break;
			default:
				break;
		}
		return expression_FunctionAppWithExprArg;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		
		//mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_y", RuntimeFunctions.cart_ySig,false);
		switch(expression_FunctionAppWithIndexArg.function)
		{
			case KW_cart_x:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_x", RuntimeFunctions.cart_xSig,false);
				break;
			case KW_cart_y:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_y", RuntimeFunctions.cart_ySig,false);
				break;
			case KW_polar_r:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_r", RuntimeFunctions.polar_rSig,false);
				break;
			case KW_polar_a:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_a", RuntimeFunctions.polar_aSig,false);
				break;
			default:
				break;
		}
		
		return expression_FunctionAppWithIndexArg;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		switch(expression_PredefinedName.kind)
		{
		
			case KW_a: 
				mv.visitFieldInsn(GETSTATIC,className,"x",CodeGenUtils.getJVMType(Type.INTEGER));
				mv.visitFieldInsn(GETSTATIC,className,"y",CodeGenUtils.getJVMType(Type.INTEGER));
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_a", RuntimeFunctions.polar_aSig,false);
				mv.visitInsn(DUP);
				mv.visitFieldInsn(PUTSTATIC, className, "a", CodeGenUtils.getJVMType(Type.INTEGER));
				//mv.visitFieldInsn(GETSTATIC, className, "a", CodeGenUtils.getJVMType(Type.INTEGER));
				
				break;
			case KW_r:
				mv.visitFieldInsn(GETSTATIC,className,"x",CodeGenUtils.getJVMType(Type.INTEGER));
				mv.visitFieldInsn(GETSTATIC,className,"y",CodeGenUtils.getJVMType(Type.INTEGER));
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_r", RuntimeFunctions.polar_rSig,false);
				mv.visitInsn(DUP);
				mv.visitFieldInsn(PUTSTATIC, className, "r", CodeGenUtils.getJVMType(Type.INTEGER));
				
				break;
			case KW_x:
				mv.visitFieldInsn(GETSTATIC,className,"x",CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			case KW_y:
				mv.visitFieldInsn(GETSTATIC,className,"y",CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			case KW_X:
				//mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getX", ImageSupport.getXSig,false);
				//mv.visitInsn(DUP);
				//mv.visitFieldInsn(PUTSTATIC, className, "X", CodeGenUtils.getJVMType(Type.INTEGER));
				mv.visitFieldInsn(GETSTATIC,className,"X",CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			case KW_Y:
				//mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getY", ImageSupport.getYSig,false);
				//mv.visitInsn(DUP);
				//mv.visitFieldInsn(PUTSTATIC, className, "Y", CodeGenUtils.getJVMType(Type.INTEGER));
				mv.visitFieldInsn(GETSTATIC,className,"Y",CodeGenUtils.getJVMType(Type.INTEGER));
				
				break;
			case KW_Z:
				mv.visitFieldInsn(GETSTATIC,className,"Z",CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			case KW_DEF_X:
				mv.visitFieldInsn(GETSTATIC,className,"DEF_X",CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			case KW_DEF_Y:
				mv.visitFieldInsn(GETSTATIC,className,"DEF_Y",CodeGenUtils.getJVMType(Type.INTEGER));
				break;
			default:
				break;
		
				
		}
		
		return expression_PredefinedName;

		//throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		//statement_Out.sink.visit(this, null);
		Declaration dec = statement_Out.getDec();
		if(dec.agType == Type.INTEGER || dec.agType == Type.BOOLEAN){
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, CodeGenUtils.getJVMType(dec.agType));
			CodeGenUtils.genLogTOS(GRADE, mv, dec.agType);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" +  CodeGenUtils.getJVMType(dec.agType) + ")V",false);
		}
		else if(dec.agType == Type.IMAGE){
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, CodeGenUtils.getJVMType(Type.IMAGE));
			CodeGenUtils.genLogTOS(GRADE, mv, dec.agType);			
			statement_Out.sink.visit(this, arg);
		}
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
			case IMAGE:{
				Declaration_Image declaration_Image = (Declaration_Image) statement_In.getDec();
				if(declaration_Image.xSize != null && declaration_Image.ySize != null)
				{
					mv.visitFieldInsn(GETSTATIC,className,statement_In.name, CodeGenUtils.getJVMType(Type.IMAGE));
					mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getX", ImageSupport.getXSig,false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitFieldInsn(GETSTATIC,className,statement_In.name, CodeGenUtils.getJVMType(Type.IMAGE));
					mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getY", ImageSupport.getYSig,false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				else
				{
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig,false);
				mv.visitFieldInsn(PUTSTATIC,className,statement_In.name, CodeGenUtils.getJVMType(Type.IMAGE));
						
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
		if(statement_Assign.lhs.agType.equals(TypeUtils.Type.INTEGER)||statement_Assign.lhs.agType.equals(TypeUtils.Type.BOOLEAN))
		{
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		else if(statement_Assign.lhs.agType == Type.IMAGE)
		{
			Label  innerLoopStart = new Label();
			Label  innerLoopEnd = new Label();
			Label  outerLoopStart = new Label();
			Label  outerLoopEnd = new Label();
		
			mv.visitFieldInsn(GETSTATIC,className,statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getX", ImageSupport.getXSig,false);
			mv.visitFieldInsn(PUTSTATIC,className,"X", CodeGenUtils.getJVMType(Type.INTEGER));
			
			mv.visitFieldInsn(GETSTATIC,className,statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getY", ImageSupport.getYSig,false);
			mv.visitFieldInsn(PUTSTATIC,className,"Y", CodeGenUtils.getJVMType(Type.INTEGER));
			
			
			mv.visitLdcInsn(0);
			//Outer loop start:
			mv.visitLabel(outerLoopStart);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC,className,"x", CodeGenUtils.getJVMType(Type.INTEGER));
			mv.visitFieldInsn(GETSTATIC,className,"X", CodeGenUtils.getJVMType(Type.INTEGER));
			
			mv.visitJumpInsn(IF_ICMPEQ, outerLoopEnd);
			
			
			mv.visitLdcInsn(0);			
			//inner  loop start:
			mv.visitLabel(innerLoopStart);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC,className,"y", CodeGenUtils.getJVMType(Type.INTEGER));
			mv.visitFieldInsn(GETSTATIC,className,"Y", CodeGenUtils.getJVMType(Type.INTEGER));
			mv.visitJumpInsn(IF_ICMPEQ, innerLoopEnd);		
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);	
			mv.visitJumpInsn(GOTO, innerLoopStart);
			
			mv.visitLabel(innerLoopEnd);
			mv.visitInsn(POP);
			
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitJumpInsn(GOTO, outerLoopStart);
			
			mv.visitLabel(outerLoopEnd);
			mv.visitInsn(POP);
			
		}

		return statement_Assign;
		//throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		
		if(lhs.agType == Type.INTEGER || lhs.agType == Type.BOOLEAN){
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, CodeGenUtils.getJVMType(lhs.agType));
		}
		else  if(lhs.agType==Type.IMAGE)
		{
			//check
			mv.visitFieldInsn(GETSTATIC,className,lhs.name, CodeGenUtils.getJVMType(Type.IMAGE));
			mv.visitFieldInsn(GETSTATIC,className,"x", CodeGenUtils.getJVMType(Type.INTEGER));
			mv.visitFieldInsn(GETSTATIC,className,"y", CodeGenUtils.getJVMType(Type.INTEGER));
			//lhs.index.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"setPixel", ImageSupport.setPixelSig,false);//invokes the virtual print
			
		}
		return lhs;
		//throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className,"makeFrame", ImageSupport.makeFrameSig,false);
		mv.visitInsn(POP);
		return sink_SCREEN;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		//check
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, CodeGenUtils.getJVMType(Type.STRING));
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"write",ImageSupport.writeSig,false);//invokes the virtual print
	
		return sink_Ident;
		//throw new UnsupportedOperationException();
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
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
 		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, CodeGenUtils.getJVMType(expression_Ident.agType));
		
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.agType);
		return expression_Ident;
	}

}
