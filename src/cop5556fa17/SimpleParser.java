package cop5556fa17;



import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParser {

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

    SimpleParser(Scanner scanner) {
        this.scanner = scanner;
        t = scanner.nextToken();
    }




    /**
     * Main method called by compiler to parser input.
     * Checks for EOF
     * 
     * @throws SyntaxException
     */
    public void parse() throws SyntaxException {
        program();
        matchEOF();
    }


    /**
     * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
     * 
     * Program is start symbol of our grammar.
     * 
     * @throws SyntaxException
     */
    void program() throws SyntaxException {
        //Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*  
        //TODO  implement this

        match(Kind.IDENTIFIER);
        while (CheckFirst(NonTerminal.Declaration) || CheckFirst(NonTerminal.Statement)) {
        	if(CheckFirst(NonTerminal.Declaration)){
        		declaration();
        		match(Kind.SEMI);
        	}
        	else{
        		statement();
        		match(Kind.SEMI);
        	}
        }
        //throw new UnsupportedOperationException();
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

    void declaration() throws SyntaxException {
        if (CheckFirst(NonTerminal.Variable_Declaration)) {
            variableDeclaration();
        } else if (CheckFirst(NonTerminal.Image_Declaration)) {
            imageDeclaration();
        } else if (CheckFirst(NonTerminal.Source_Sink_Declaration)) {
            sourceSinkDeclaration();
        } else {
            throw new SyntaxException(t, "Declaration issue");
        }


    }

    void sourceSinkDeclaration() throws SyntaxException {
        sourceSinkType();
        match(Kind.IDENTIFIER);
        match(Kind.OP_ASSIGN);
        source();
    }
    public void sourceSinkType() throws SyntaxException {
        if (t.isKind(Kind.KW_url))
            match(Kind.KW_url);
        else if (t.isKind(Kind.KW_file))
            match(Kind.KW_file);
        else
            throw new SyntaxException(t, "Invalid SourceSinkType");
    }
    
    void imageDeclaration() throws SyntaxException {
        match(Kind.KW_image);
        if (t.isKind(Kind.LSQUARE)) {
            match(Kind.LSQUARE);
            expression();
            match(Kind.COMMA);
            expression();
            match(Kind.RSQUARE);
        }
        match(Kind.IDENTIFIER);
        if (t.isKind(Kind.OP_LARROW)) {
            match(Kind.OP_LARROW);
            source();
        }
    }
    void source() throws SyntaxException {
        if (t.isKind(Kind.STRING_LITERAL)) {
            match(Kind.STRING_LITERAL);
        } else if (t.isKind(Kind.OP_AT)) {
            match(Kind.OP_AT);
            expression();
        } else if (t.isKind(Kind.IDENTIFIER)) {
            match(Kind.IDENTIFIER);
        } else {
            throw new SyntaxException(t, "issue in source terminal");
        }
    }
    void variableDeclaration() throws SyntaxException {
        varType();
        match(Kind.IDENTIFIER);
        if (t.isKind(Kind.OP_ASSIGN)) {
            match(Kind.OP_ASSIGN);
            expression();
        }

    }
    
    void statement() throws SyntaxException{
    	match(Kind.IDENTIFIER);
    	if(CheckFirst(NonTerminal.Assignment_Statement)){
    		assignmentStatement();
    	}
    	else if(CheckFirst(NonTerminal.Image_In_Statement)){
    		imageInStatement();
    	}
    	else if(CheckFirst(NonTerminal.Image_Out_Statement)){
    		imageOutStatement();
    	}
    	else{
    		throw new SyntaxException(t, "statement error");
    	}
    }

    void imageOutStatement() throws SyntaxException {
        //match(Kind.IDENTIFIER);
        match(Kind.OP_RARROW);
        sink();
    }

    void imageInStatement() throws SyntaxException {
        //match(Kind.IDENTIFIER);
        match(Kind.OP_LARROW);
        source();
    }

    void assignmentStatement() throws SyntaxException {
        lhs();
        match(Kind.OP_ASSIGN);
        expression();

    }

    void lhs() throws SyntaxException {
        //match(Kind.IDENTIFIER);
        if (t.isKind(Kind.LSQUARE)) {
            match(Kind.LSQUARE);
            lhsSelector();
            match(Kind.RSQUARE);
        }
    }




    public void functionApplication() throws SyntaxException {
        functionName();
        switch (t.kind) {
            case LPAREN:
                {
                    match(Kind.LPAREN);expression();match(Kind.RPAREN);
                }
                break;
            case LSQUARE:
                {
                    match(Kind.LSQUARE);selector();match(Kind.RSQUARE);
                }
                break;
            default:
                throw new SyntaxException(t, "Invalid Function Application");
        }
    }

    public void functionName() throws SyntaxException {
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
    }

    public void lhsSelector() throws SyntaxException {
        match(Kind.LSQUARE);
        if (CheckFirst(NonTerminal.XySelector)) {
            xySelector();
        } else if (CheckFirst(NonTerminal.RaSelector)) {
            raSelector();
        } else throw new SyntaxException(t, "Invalid Selector choice");
        match(Kind.RSQUARE);
    }

    public void xySelector() throws SyntaxException {
        match(Kind.KW_x);
        match(Kind.COMMA);
        match(Kind.KW_y);
    }

    public void raSelector() throws SyntaxException {
        match(Kind.KW_r);
        match(Kind.COMMA);
        match(Kind.KW_A);
    }

    public void selector() throws SyntaxException {
        
        expression();
        match(Kind.COMMA);
        expression();
    }




    void sink() throws SyntaxException {
        // TODO Auto-generated method stub
        if (t.isKind(Kind.IDENTIFIER)) {
            /*if(t.getText().equals("file")){
            	match(Kind.IDENTIFIER); // ident must be file
            }
            else{
            	throw new SyntaxException(t,"not fiile");
            }   */     	
            match(Kind.IDENTIFIER);
        } else if (t.isKind(Kind.KW_SCREEN)) {
            match(Kind.KW_SCREEN);
        } else {
            throw new SyntaxException(t, "Sink error");
        }
    }




    void varType() throws SyntaxException {
        if (t.isKind(Kind.KW_int)) {
            match(Kind.KW_int);
        } else if (t.isKind(Kind.KW_boolean)) {
            match(Kind.KW_boolean);
        } else {
            throw new SyntaxException(t, "vartype error");
        }

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
    
    public void expression() throws SyntaxException
	{
		//Merge all the expressions text here
		orExpression();
		if(t.isKind(Kind.OP_Q))
		{
			match(Kind.OP_Q);
			expression();
			match(Kind.OP_COLON);
			expression();
		}
		//throw new UnsupportedOperationException();
	}
	
	public void orExpression() throws SyntaxException
	{
		andExpression();
		while(t.isKind(Kind.OP_OR))
		{
			match(Kind.OP_OR);
			andExpression();
		}
		
	}
	
	public void andExpression() throws SyntaxException
	{
		eqExpression();
		while(t.isKind(Kind.OP_AND))
		{
			match(Kind.OP_AND);
			eqExpression();
		}
		
	}
	
	public void eqExpression() throws SyntaxException
	{
		relExpression();
		while(t.isKind(Kind.OP_EQ)||t.isKind(Kind.OP_NEQ))
		{
			if(t.isKind(Kind.OP_EQ))
			{
				match(Kind.OP_EQ);
			}
			else if(t.isKind(Kind.OP_NEQ))
			{
				match(Kind.OP_NEQ);
			}
			relExpression();
		}	
	}
	
	public void relExpression() throws SyntaxException
	{
		addExpression();
		while(t.isKind(Kind.OP_LT)||t.isKind(Kind.OP_GT)||t.isKind(Kind.OP_LE)||t.isKind(Kind.OP_GE))
		{
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
			addExpression();
		}
	}
	
	
	public void addExpression() throws SyntaxException
	{
		multExpression();
		while(t.isKind(Kind.OP_PLUS)||t.isKind(Kind.OP_MINUS))
		{
			if(t.isKind(Kind.OP_PLUS))
			{
				match(Kind.OP_PLUS);
			}
			else if(t.isKind(Kind.OP_MINUS))
			{
				match(Kind.OP_MINUS);	
			}
			multExpression();
		}
		
		
	}
	
	public void multExpression() throws SyntaxException
	{
		unaryExpression();
		while(t.isKind(Kind.OP_TIMES)||t.isKind(Kind.OP_DIV)||t.isKind(Kind.OP_MOD))
		{
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
			unaryExpression();
		}
	}
	
	public void unaryExpression() throws SyntaxException
	{
		// TODO Auto-generated method stub
		if(t.isKind(Kind.OP_PLUS)) 
		{
			match(Kind.OP_PLUS);
			unaryExpression();
		}
		else if(t.isKind(Kind.OP_MINUS))
		{
			match(OP_MINUS);
			unaryExpression();
		}
		else{
			unaryExpressionNotPlusMinus();
		}
			
	}
	
	public void unaryExpressionNotPlusMinus() throws SyntaxException
	{
		if(CheckFirst(NonTerminal.Primary))
		{
			primary();
		}
		else if(CheckFirst(NonTerminal.IdentOrPixelSelectorExpression))
		{
			 identOrPixelSelectorExpression();
		}
		
		else
		{
			switch(t.kind)
			{
				case OP_EXCL: { match(Kind.OP_EXCL); unaryExpression();} break;
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
	}
	
	
	public void primary() throws SyntaxException
	{
		if(t.isKind(Kind.INTEGER_LITERAL))
			match(Kind.INTEGER_LITERAL);
		else if(t.isKind(Kind.BOOLEAN_LITERAL)){
			match(Kind.BOOLEAN_LITERAL);
		}
		else if(t.isKind(Kind.LPAREN))
		{
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
		}
		else 
		{
			functionApplication();
		}
		
	}
	
	public void identOrPixelSelectorExpression() throws SyntaxException
	{
		match(Kind.IDENTIFIER);
		if(t.isKind(Kind.LSQUARE))
		{
			match(Kind.LSQUARE);
			selector();
			match(Kind.RSQUARE);
		}
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