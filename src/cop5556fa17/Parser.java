package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.AST.*;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

    @SuppressWarnings("serial")
    public class SyntaxException extends Exception {
        Token t;

        public SyntaxException(Token t, String message) {
            super(message);
            this.t = t;
        }

    }


    Scanner scanner;
    Token t;

    Parser(Scanner scanner) {
        this.scanner = scanner;
        t = scanner.nextToken();
    }




    /**
     * Main method called by compiler to parser input.
     * Checks for EOF
     * 
     * @throws SyntaxException
     */
    public Program parse() throws SyntaxException {
        Program prog = program();
        matchEOF();
        return prog;
    }


    /**
     * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
     * 
     * Program is start symbol of our grammar.
     * 
     * @throws SyntaxException
     */
    Program program() throws SyntaxException {
        //Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*  
        //TODO  implement this
    	Token cur = t;
        match(Kind.IDENTIFIER);
        //Token name = t;
        ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>(); 
        
        while (CheckFirst(NonTerminal.Declaration) || CheckFirst(NonTerminal.Statement)) {
        	if(CheckFirst(NonTerminal.Declaration)){
        		ASTNode dec = declaration();
        		match(Kind.SEMI);
        		decsAndStatements.add(dec);
        	}
        	else{
        		ASTNode stmt = statement();
        		match(Kind.SEMI);
        		decsAndStatements.add(stmt);
        	}
        }
        //throw new UnsupportedOperationException();
        return new Program(cur, cur, decsAndStatements);
    }

    public static enum NonTerminal {
        Variable_Declaration,
        Image_Declaration,
        Source_Sink_Declaration,
        Assignment_Statement,
        Image_Out_Statement,
        Image_In_Statement,
        XySelector,
        RaSelector,
        Declaration,
        Statement, FunctionApplication, UnaryExpressionNotPlusMinus, IdentOrPixelSelectorExpression, Primary;
    }

    boolean CheckFirst(NonTerminal type) {
        boolean retVal = false;


        switch (type) {

            case Variable_Declaration:
                retVal = t.isKind(Kind.KW_int) || t.isKind(Kind.KW_boolean);
                break;
            case Image_Declaration:
                retVal = t.isKind(Kind.KW_image);
                break;
            case Source_Sink_Declaration:
                retVal = t.isKind(Kind.KW_url) || t.isKind(KW_file);
                break;
            case Image_In_Statement:
            	retVal = t.isKind(Kind.OP_LARROW);
            	break;
            case Image_Out_Statement:
            	retVal = t.isKind(Kind.OP_RARROW); break;
            case Assignment_Statement:
            	retVal = t.isKind(Kind.LSQUARE) || t.isKind(Kind.OP_ASSIGN); break;
            case Declaration:
            	retVal = CheckFirst(NonTerminal.Variable_Declaration) || CheckFirst(NonTerminal.Image_Declaration) || CheckFirst(NonTerminal.Source_Sink_Declaration);
            	break;
            case Statement:
            	retVal = t.isKind(Kind.IDENTIFIER);//CheckFirst(NonTerminal.Assignment_Statement) || CheckFirst(NonTerminal.Image_In_Statement) || CheckFirst(NonTerminal.Image_Out_Statement);
            	break;       
            case XySelector:
            	retVal = t.isKind(Kind.KW_x);
            	break;
            case RaSelector: 
            	retVal = t.isKind(Kind.KW_r);
            	break;
            case IdentOrPixelSelectorExpression:
            	retVal = t.isKind(Kind.IDENTIFIER);
            	break;
            case Primary:
            	retVal = t.isKind(Kind.INTEGER_LITERAL) || t.isKind(Kind.LPAREN) || t.isKind(Kind.BOOLEAN_LITERAL) || CheckFirst(NonTerminal.FunctionApplication);
            	break;
            case FunctionApplication: 
            	retVal = t.isKind(Kind.KW_sin) || t.isKind(Kind.KW_cos) ||t.isKind(Kind.KW_atan) ||t.isKind(Kind.KW_abs) ||
            			t.isKind(Kind.KW_cart_x) || t.isKind(Kind.KW_cart_y) ||t.isKind(Kind.KW_polar_a) ||t.isKind(Kind.KW_polar_r) ;

            	break;
           
        }
        return retVal;
    }

    ASTNode declaration() throws SyntaxException {
        ASTNode n;
    	if (CheckFirst(NonTerminal.Variable_Declaration)) {
             n = variableDeclaration();
        } else if (CheckFirst(NonTerminal.Image_Declaration)) {
            n = imageDeclaration();
        } else if (CheckFirst(NonTerminal.Source_Sink_Declaration)) {
           n = sourceSinkDeclaration();
        } else {
            throw new SyntaxException(t, "Declaration issue");
        }
    	return n;

    }

    Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
        Token cur = t;
        Token type = sourceSinkType();
        Token name = t;
        match(Kind.IDENTIFIER);
        match(Kind.OP_ASSIGN);
        Source src = source();
        return new Declaration_SourceSink(cur, type, name, src);
    }
    public Token sourceSinkType() throws SyntaxException {
    	Token cur = null;
        if(t.isKind(Kind.KW_url)){
        	cur = t;
            match(Kind.KW_url);
        }
        else if (t.isKind(Kind.KW_file)){
            cur = t;
        	match(Kind.KW_file);
        }
        else
            throw new SyntaxException(t, "Invalid SourceSinkType");
        
        return cur;
    }
    
    Declaration_Image imageDeclaration() throws SyntaxException {
        Token cur  = t;
        Expression xSize = null,ySize = null;
        Source src = null;
    	match(Kind.KW_image);
        if (t.isKind(Kind.LSQUARE)) {
            match(Kind.LSQUARE);
            xSize = expression();
            match(Kind.COMMA);
            ySize = expression();
            match(Kind.RSQUARE);
        }
        Token name = t;
        match(Kind.IDENTIFIER);
        if (t.isKind(Kind.OP_LARROW)) {
            match(Kind.OP_LARROW);
            src = source();
        }
        return new Declaration_Image(cur, xSize, ySize, name, src);
    }
    Source source() throws SyntaxException {
    	Token cur = t;
    	Source retVal = null;
        if (t.isKind(Kind.STRING_LITERAL)) {
        	String str = t.getText();
            match(Kind.STRING_LITERAL);
            retVal = new Source_StringLiteral(cur, str);
        } else if (t.isKind(Kind.OP_AT)) {
            match(Kind.OP_AT);
            Expression e0 = expression();
            retVal = new Source_CommandLineParam(cur, e0);
        } else if (t.isKind(Kind.IDENTIFIER)) {
        	Token name = t;
            match(Kind.IDENTIFIER);
            retVal = new Source_Ident(cur, name);
        } else {
            throw new SyntaxException(t, "issue in source terminal");
        }
        return retVal;
    }
    Declaration_Variable variableDeclaration() throws SyntaxException {
        Token cur = t;
        
    	Token varToken = varType();
    	Token name = t;
        match(Kind.IDENTIFIER);
        Expression exp = null;
        if (t.isKind(Kind.OP_ASSIGN)) {
            match(Kind.OP_ASSIGN);
            exp = expression();
        }
        return new Declaration_Variable(cur, varToken, name, exp);
    }
    
    ASTNode statement() throws SyntaxException{
    	ASTNode retVal = null;
    	Token cur = t;
    	match(Kind.IDENTIFIER);
    	
    	if(CheckFirst(NonTerminal.Assignment_Statement)){
    		retVal = assignmentStatement(cur);
    	}
    	else if(CheckFirst(NonTerminal.Image_In_Statement)){
    		retVal = imageInStatement(cur);
    	}
    	else if(CheckFirst(NonTerminal.Image_Out_Statement)){
    		retVal = imageOutStatement(cur);
    	}
    	else{
    		throw new SyntaxException(t, "statement error");
    	}
    	return retVal;
    }

    Statement imageOutStatement(Token name) throws SyntaxException {
        //match(Kind.IDENTIFIER);
    	Token cur = t;
    	Statement retVal = null;
        match(Kind.OP_RARROW);
       
        Sink snk = sink();
        return new Statement_Out(name, name, snk);
    }

    Statement imageInStatement(Token name) throws SyntaxException {
        //match(Kind.IDENTIFIER);
    	Token cur = t;
    	Statement retVal = null;
        match(Kind.OP_LARROW);
       
        Source src = source();
        return new Statement_In(name, name, src);
    }

    Statement assignmentStatement(Token name) throws SyntaxException {
       Statement_Assign ret = null;
       Token cur = t;
    	LHS lh = lhs(name);
        match(Kind.OP_ASSIGN);
        Expression e0 = expression();
        return new Statement_Assign(name, lh, e0);
    }

    LHS lhs(Token idToken) throws SyntaxException {
        //match(Kind.IDENTIFIER);
    	
    	
    	Index idx = null;
        if (t.isKind(Kind.LSQUARE)) {
            match(Kind.LSQUARE);
            
            idx = lhsSelector();
            match(Kind.RSQUARE);
        }
        return new LHS(idToken, idToken, idx);
    }




    public Expression functionApplication() throws SyntaxException {
        Token cur = t;
    	Kind funName = functionName();
    	Expression retVal = null;
        switch (t.kind) {
            case LPAREN:
                {
                    match(Kind.LPAREN);                    
                    Expression e1 = expression();
                    match(Kind.RPAREN);
                    retVal = new Expression_FunctionAppWithExprArg(cur, funName, e1);
                }
                break;
            case LSQUARE:
                {
                    match(Kind.LSQUARE);                    
                    Index idx = selector();
                    match(Kind.RSQUARE);
                    retVal = new Expression_FunctionAppWithIndexArg(cur, funName, idx);
                }
                break;
            default:
                throw new SyntaxException(t, "Invalid Function Application");
        }
        return retVal;
    }

    public Kind functionName() throws SyntaxException {
        Token cur = t;
    	switch (t.kind) {
            case KW_sin:
                match(Kind.KW_sin);
                break;
            case KW_cos:
                match(Kind.KW_cos);
                break;
            case KW_atan:
                match(Kind.KW_atan);
                break;
            case KW_abs:
                match(Kind.KW_abs);
                break;
            case KW_cart_x:
                match(Kind.KW_cart_x);
                break;
            case KW_cart_y:
                match(Kind.KW_cart_y);
                break;
            case KW_polar_a:
                match(Kind.KW_polar_a);
                break;
            case KW_polar_r:
                match(Kind.KW_polar_r);
                break;
            default:
                throw new SyntaxException(t, "Invalid Function Name");
                
        }
    	
    	return cur.kind;
    }

    public Index lhsSelector() throws SyntaxException {
       
    	match(Kind.LSQUARE);
    	Index idx = null;
    	
        if (CheckFirst(NonTerminal.XySelector)) {
            idx = xySelector();
        } else if (CheckFirst(NonTerminal.RaSelector)) {
            idx = raSelector();
        } else throw new SyntaxException(t, "Invalid Selector choice");
        match(Kind.RSQUARE);
        return idx;
    }

    public Index xySelector() throws SyntaxException {
        Token cur = t;
        Expression e0 = new Expression_PredefinedName(t,Kind.KW_x);
    	match(Kind.KW_x);
    	
        match(Kind.COMMA);
        Expression e1 = new Expression_PredefinedName(cur,Kind.KW_y);
        match(Kind.KW_y);
        
        return new Index(cur, e0, e1);//check null;
    }

    public Index raSelector() throws SyntaxException {
        Token cur = t;
        Expression e0 = new Expression_PredefinedName(t, Kind.KW_r);
    	match(Kind.KW_r);
    	
        match(Kind.COMMA);
        Expression e1 = new Expression_PredefinedName(t, Kind.KW_a);
        match(Kind.KW_a);
        return new Index(cur,e0,e1);//check null
    }

    public Index selector() throws SyntaxException {
        Token cur = t;
        Expression e0 = expression();
        match(Kind.COMMA);
        Expression e1 = expression();
        return new Index(cur, e0, e1);
    }




    Sink sink() throws SyntaxException {
        // TODO Auto-generated method stub
    	Token cur = t;
    	Sink retVal = null;
        if (t.isKind(Kind.IDENTIFIER)) {
            /*if(t.getText().equals("file")){
            	match(Kind.IDENTIFIER); // ident must be file
            }
            else{
            	throw new SyntaxException(t,"not fiile");
            }   */  
        	Token name = t;
            match(Kind.IDENTIFIER);
            retVal = new Sink_Ident(cur, name);
        } else if (t.isKind(Kind.KW_SCREEN)) {
            match(Kind.KW_SCREEN);
            retVal = new Sink_SCREEN(cur);
        } else {
            throw new SyntaxException(t, "Sink error");
        }
        return retVal;
    }




    Token varType() throws SyntaxException {
    	Token cur = t;
        if (t.isKind(Kind.KW_int)) {
            match(Kind.KW_int);
        } else if (t.isKind(Kind.KW_boolean)) {
            match(Kind.KW_boolean);
        } else {
            throw new SyntaxException(t, "vartype error");
        }

        return cur;
    }

    /**
     * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
     * 
     * Our test cases may invoke this routine directly to support incremental development.
     * 
     * @throws SyntaxException
     */
    

    void consume() {
        t = scanner.nextToken();
    }

    void match(Kind kind) throws SyntaxException {
        if (t.isKind(kind)) {
            consume();
        } else {
            throw new SyntaxException(t, "hello keyur");
        }

        //handle error
    }
    
    public Expression expression() throws SyntaxException
	{
    	Token cur = t;
    	Expression returnExp = null;
		//Merge all the expressions text here
    	//Expression_Conditional exp_conditional = null;
    	returnExp = orExpression();
		if(t.isKind(Kind.OP_Q))
		{
			match(Kind.OP_Q);
			Expression e1 = expression();
			match(Kind.OP_COLON);
			Expression e2 = expression();
			return new Expression_Conditional(cur,returnExp,e1,e2);
		}
		return returnExp;
		//throw new UnsupportedOperationException();
	}
	
	public Expression orExpression() throws SyntaxException
	{
		Token cur = t;
		Token op = null;
		Expression e0 = null;
		e0 = andExpression();
		while(t.isKind(Kind.OP_OR))
		{
			
			op = t;
			match(Kind.OP_OR);
			Expression e1 = andExpression();
			e0 = new Expression_Binary(cur, e0, op, e1);//check cur
		}
		return e0;
	}
	
	public Expression andExpression() throws SyntaxException
	{
		Token cur = t;
		Token op = null;
		Expression e0 = eqExpression();
		while(t.isKind(Kind.OP_AND))
		{
			op = t;
			match(Kind.OP_AND);
			Expression e1 = eqExpression();
			e0 = new Expression_Binary(cur, e0, op, e1);
		}
		return e0;
	}
	
	public Expression eqExpression() throws SyntaxException
	{
		Token cur = t;
		Expression e0 = relExpression();
		while(t.isKind(Kind.OP_EQ)||t.isKind(Kind.OP_NEQ))
		{
			Token op = t;
			if(t.isKind(Kind.OP_EQ))
			{
				match(Kind.OP_EQ);
			}
			else if(t.isKind(Kind.OP_NEQ))
			{
				match(Kind.OP_NEQ);
			}
			Expression e1 = relExpression();
			e0 = new Expression_Binary(cur, e0, op, e1);
		}	
		return e0;
	}
	
	public Expression relExpression() throws SyntaxException
	{
		Token cur = t;
		Expression e0 = addExpression();
		while(t.isKind(Kind.OP_LT)||t.isKind(Kind.OP_GT)||t.isKind(Kind.OP_LE)||t.isKind(Kind.OP_GE))
		{
			Token op = t;
			if(t.isKind(Kind.OP_LT))
			{
				match(Kind.OP_LT);
			}
			else if(t.isKind(Kind.OP_GT))
			{
				match(Kind.OP_GT);
			}
			else if(t.isKind(Kind.OP_LE))
			{
				match(Kind.OP_LE);
			}
			else if(t.isKind(Kind.OP_GE))
			{
				match(Kind.OP_GE);
			}
			Expression e1 = addExpression();
			e0 = new Expression_Binary(cur, e0, op, e1);//check
		}
		return e0;
	}
	
	
	public Expression addExpression() throws SyntaxException
	{
		Token cur = t;
		Expression e0 = multExpression();
		while(t.isKind(Kind.OP_PLUS)||t.isKind(Kind.OP_MINUS))
		{
			Token op = t;
			if(t.isKind(Kind.OP_PLUS))
			{
				match(Kind.OP_PLUS);
			}
			else if(t.isKind(Kind.OP_MINUS))
			{
				match(Kind.OP_MINUS);	
			}
			Expression e1 = multExpression();
			e0 = new Expression_Binary(cur, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression multExpression() throws SyntaxException
	{
		Token cur = t;
		Expression e0 = unaryExpression();
		while(t.isKind(Kind.OP_TIMES)||t.isKind(Kind.OP_DIV)||t.isKind(Kind.OP_MOD))
		{
			Token op = t;
			if(t.isKind(Kind.OP_TIMES))
			{
				match(Kind.OP_TIMES);
			}
			else if(t.isKind(Kind.OP_DIV))
			{
				match(Kind.OP_DIV);
			}
			else if(t.isKind(Kind.OP_MOD))
			{
				match(Kind.OP_MOD);
			}
			Expression e1 = unaryExpression();
			e0 = new Expression_Binary(cur, e0, op, e1);
		}
		return e0;
	}
	
	public Expression unaryExpression() throws SyntaxException
	{
		Token cur = t;
		Expression e0 = null;
		Token op = null;
		// TODO Auto-generated method stub
		if(t.isKind(Kind.OP_PLUS)) 
		{
			op = t;
			match(Kind.OP_PLUS);
			e0 = new Expression_Unary(cur, op, unaryExpression());
		}
		else if(t.isKind(Kind.OP_MINUS))
		{
			op = t;
			match(OP_MINUS);
			e0 = new Expression_Unary(cur, op, unaryExpression());
		}
		else{
			e0 = unaryExpressionNotPlusMinus();
		}
		return e0;
			
	}
	
	public Expression unaryExpressionNotPlusMinus() throws SyntaxException
	{
		Token cur = t;
		Expression exp = null;
		if(CheckFirst(NonTerminal.Primary))
		{	
			return primary();
		}
		else if(CheckFirst(NonTerminal.IdentOrPixelSelectorExpression))
		{
			 return identOrPixelSelectorExpression();
		}
		
		else
		{
			switch(t.kind)
			{
				case OP_EXCL: { 
				Token op = t;	
				match(Kind.OP_EXCL); 
				 
			    Expression ex = unaryExpression();
			    exp = new Expression_Unary(cur, op, ex);
				} break;
				case KW_x: match(Kind.KW_x); break;
				case KW_y: match( Kind.KW_y ); break;
				case KW_r: match( Kind.KW_r ); break;
				case KW_a: match( Kind.KW_a ); break;
				case KW_X: match( Kind.KW_X ); break;
				case KW_Y: match( Kind.KW_Y ); break;
				case KW_Z: match( Kind.KW_Z ); break;
				case KW_A: match( Kind.KW_A ); break;
				case KW_R: match( Kind.KW_R ); break;
				case KW_DEF_X: match( Kind.KW_DEF_X ); break;
				case KW_DEF_Y: match( Kind.KW_DEF_Y ); break;
				default:
					throw new SyntaxException(t,"Invalid Function Name");
			}	
		}
		if(exp == null){
		return new Expression_PredefinedName(cur, cur.kind);
		}
		else return exp;
	}
	
	
	public Expression primary() throws SyntaxException
	{
		Token cur = t;
		Expression retVal = null;
		if(t.isKind(Kind.INTEGER_LITERAL)){
			match(Kind.INTEGER_LITERAL);
			return new Expression_IntLit(cur, cur.intVal());
		}
		
		else if(t.isKind(Kind.BOOLEAN_LITERAL)){
			match(Kind.BOOLEAN_LITERAL);
			boolean boolVal = false;
			if(cur.getText().equals("true")) boolVal = true;
			return new Expression_BooleanLit(cur, boolVal);
		}
		else if(t.isKind(Kind.LPAREN))
		{
			match(Kind.LPAREN);
			retVal = expression(); ///check
			match(Kind.RPAREN);
		}
		else 
		{
			retVal = functionApplication();
		}
		return retVal;
	}
	
	public Expression identOrPixelSelectorExpression() throws SyntaxException
	{
		Expression retVal = null;
		Token cur = t;
		match(Kind.IDENTIFIER);
		retVal = new Expression_Ident(cur, cur);
		if(t.isKind(Kind.LSQUARE))
		{
			match(Kind.LSQUARE);
			
			Index idx = selector();
			match(Kind.RSQUARE);
			retVal = new Expression_PixelSelector(cur, cur, idx);
		}
		return retVal;
	}

    /**
     * Only for check at end of program. Does not "consume" EOF so no attempt to get
     * nonexistent next Token.
     * 
     * @return
     * @throws SyntaxException
     */
    public Token matchEOF() throws SyntaxException {
        if (t.kind == EOF) {
            return t;
        }
        String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
        throw new SyntaxException(t, message);
    }



}