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
        		Declaration();
        		match(Kind.SEMI);
        	}
        	else{
        		Statement();
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
            	retVal = t.isKind(Kind.INTEGER_LITERAL) || t.isKind(Kind.LPAREN) || CheckFirst(NonTerminal.FunctionApplication);
            	break;
            case FunctionApplication: 
            	retVal = t.isKind(Kind.KW_sin) || t.isKind(Kind.KW_cos) ||t.isKind(Kind.KW_atan) ||t.isKind(Kind.KW_abs) ||
            			t.isKind(Kind.KW_cart_x) || t.isKind(Kind.KW_cart_y) ||t.isKind(Kind.KW_polar_a) ||t.isKind(Kind.KW_polar_r) ;

            	break;
           
        }
        return retVal;
    }

    void Declaration() throws SyntaxException {
        if (CheckFirst(NonTerminal.Variable_Declaration)) {
            VariableDeclaration();
        } else if (CheckFirst(NonTerminal.Image_Declaration)) {
            ImageDeclaration();
        } else if (CheckFirst(NonTerminal.Source_Sink_Declaration)) {
            SourceSinkDeclaration();
        } else {
            throw new SyntaxException(t, "Declaration issue");
        }


    }

    void SourceSinkDeclaration() throws SyntaxException {
        SourceSinkType();
        match(Kind.IDENTIFIER);
        match(Kind.OP_ASSIGN);
        Source();
    }
    public void SourceSinkType() throws SyntaxException {
        if (t.isKind(Kind.KW_url))
            match(Kind.KW_url);
        else if (t.isKind(Kind.KW_file))
            match(Kind.KW_file);
        else
            throw new SyntaxException(t, "Invalid SourceSinkType");
    }
    
    void ImageDeclaration() throws SyntaxException {
        match(Kind.KW_image);
        if (t.isKind(Kind.LSQUARE)) {
            match(Kind.LSQUARE);
            Expression();
            match(Kind.COMMA);
            Expression();
            match(Kind.RSQUARE);
        }
        match(Kind.IDENTIFIER);
        if (t.isKind(Kind.OP_LARROW)) {
            match(Kind.OP_LARROW);
            Source();
        }
    }
    void Source() throws SyntaxException {
        if (t.isKind(Kind.STRING_LITERAL)) {
            match(Kind.STRING_LITERAL);
        } else if (t.isKind(Kind.OP_AT)) {
            match(Kind.OP_AT);
            Expression();
        } else if (t.isKind(Kind.IDENTIFIER)) {
            match(Kind.IDENTIFIER);
        } else {
            throw new SyntaxException(t, "issue in source terminal");
        }
    }
    void VariableDeclaration() throws SyntaxException {
        VarType();
        match(Kind.IDENTIFIER);
        if (t.isKind(Kind.OP_ASSIGN)) {
            match(Kind.OP_ASSIGN);
            Expression();
        }

    }
    
    void Statement() throws SyntaxException{
    	match(Kind.IDENTIFIER);
    	if(CheckFirst(NonTerminal.Assignment_Statement)){
    		AssignmentStatement();
    	}
    	else if(CheckFirst(NonTerminal.Image_In_Statement)){
    		ImageInStatement();
    	}
    	else if(CheckFirst(NonTerminal.Image_Out_Statement)){
    		ImageOutStatement();
    	}
    	else{
    		throw new SyntaxException(t, "statement error");
    	}
    }

    void ImageOutStatement() throws SyntaxException {
        //match(Kind.IDENTIFIER);
        match(Kind.OP_RARROW);
        Sink();
    }

    void ImageInStatement() throws SyntaxException {
        //match(Kind.IDENTIFIER);
        match(Kind.OP_LARROW);
        Source();
    }

    void AssignmentStatement() throws SyntaxException {
        Lhs();
        match(Kind.OP_ASSIGN);
        Expression();

    }

    void Lhs() throws SyntaxException {
        //match(Kind.IDENTIFIER);
        if (t.isKind(Kind.LSQUARE)) {
            match(Kind.LSQUARE);
            LhsSelector();
            match(Kind.RSQUARE);
        }
    }




    public void FunctionApplication() throws SyntaxException {
        FunctionName();
        switch (t.kind) {
            case LPAREN:
                {
                    match(Kind.KW_url);Expression();match(Kind.RPAREN);
                }
                break;
            case LSQUARE:
                {
                    match(Kind.LSQUARE);Selector();match(Kind.RSQUARE);
                }
                break;
            default:
                throw new SyntaxException(t, "Invalid Function Application");
        }
    }

    public void FunctionName() throws SyntaxException {
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

    public void LhsSelector() throws SyntaxException {
        match(Kind.LSQUARE);
        if (CheckFirst(NonTerminal.XySelector)) {
            XySelector();
        } else if (CheckFirst(NonTerminal.RaSelector)) {
            RaSelector();
        } else throw new SyntaxException(t, "Invalid Selector choice");
        match(Kind.RSQUARE);
    }

    public void XySelector() throws SyntaxException {
        match(Kind.KW_x);
        match(Kind.COMMA);
        match(Kind.KW_y);
    }

    public void RaSelector() throws SyntaxException {
        match(Kind.KW_r);
        match(Kind.COMMA);
        match(Kind.KW_A);
    }

    public void Selector() throws SyntaxException {
        match(Kind.KW_r);
        Expression();
        match(Kind.KW_A);
    }




    void Sink() throws SyntaxException {
        // TODO Auto-generated method stub
        if (t.isKind(Kind.IDENTIFIER)) {
            if(t.getText().equals("file")){
            	match(Kind.IDENTIFIER); // ident must be file
            }
            else{
            	throw new SyntaxException(t,"not fiile");
            }        	
            
        } else if (t.isKind(Kind.KW_SCREEN)) {
            match(Kind.KW_SCREEN);
        } else {
            throw new SyntaxException(t, "Sink error");
        }
    }




    void VarType() throws SyntaxException {
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
    
    public void Expression() throws SyntaxException
	{
		//Merge all the expressions text here
		OrExpression();
		if(t.isKind(Kind.OP_Q))
		{
			match(Kind.OP_Q);
			Expression();
			match(Kind.OP_COLON);
			Expression();
		}
		//throw new UnsupportedOperationException();
	}
	
	private void OrExpression() throws SyntaxException
	{
		AndExpression();
		while(t.isKind(Kind.OP_OR))
		{
			match(Kind.OP_OR);
			AndExpression();
		}
		
	}
	
	private void AndExpression() throws SyntaxException
	{
		EqExpression();
		while(t.isKind(Kind.OP_AND))
		{
			match(Kind.OP_AND);
			EqExpression();
		}
		
	}
	
	private void EqExpression() throws SyntaxException
	{
		RelExpression();
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
			RelExpression();
		}	
	}
	
	private void RelExpression() throws SyntaxException
	{
		AddExpression();
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
			AddExpression();
		}
	}
	
	
	private void AddExpression() throws SyntaxException
	{
		MultExpression();
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
			MultExpression();
		}
		
		
	}
	
	private void MultExpression() throws SyntaxException
	{
		UnaryExpression();
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
			UnaryExpression();
		}
	}
	
	private void UnaryExpression() throws SyntaxException
	{
		// TODO Auto-generated method stub
		if(t.isKind(Kind.OP_PLUS)) 
		{
			match(Kind.OP_PLUS);
			UnaryExpression();
		}
		else if(t.isKind(Kind.OP_MINUS))
		{
			match(OP_MINUS);
			UnaryExpression();
		}
		else{
			UnaryExpressionNotPlusMinus();
		}
			
	}
	
	public void UnaryExpressionNotPlusMinus() throws SyntaxException
	{
		if(CheckFirst(NonTerminal.Primary))
		{
			Primary();
		}
		else if(CheckFirst(NonTerminal.IdentOrPixelSelectorExpression))
		{
			 IdentOrPixelSelectorExpression();
		}
		
		else
		{
			switch(t.kind)
			{
				case OP_EXCL: { match(Kind.OP_EXCL); UnaryExpression();} break;
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
	
	
	public void Primary() throws SyntaxException
	{
		if(t.isKind(Kind.INTEGER_LITERAL))
			match(Kind.INTEGER_LITERAL);
		else if(t.isKind(Kind.LPAREN))
		{
			match(Kind.LPAREN);
			Expression();
			match(Kind.RPAREN);
		}
		else 
		{
			FunctionApplication();
		}
		
	}
	
	public void IdentOrPixelSelectorExpression() throws SyntaxException
	{
		match(Kind.IDENTIFIER);
		if(t.isKind(Kind.LSQUARE))
		{
			match(Kind.LSQUARE);
			Selector();
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
    private Token matchEOF() throws SyntaxException {
        if (t.kind == EOF) {
            return t;
        }
        String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
        throw new SyntaxException(t, message);
    }



}