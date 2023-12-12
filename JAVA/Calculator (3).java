//202121224 강지웅

import java.io.IOException;
import java.io.PushbackInputStream;
enum Relop
{
    LPAREN, RPAREN, PLUS, MINUS, MULTI, DIVIDE,
    AND, OR, NUMBER, NOT,
    EQUAL, LT, LTEQ, GT, GTEQ, NOTEQ, NLINE
};
/*  Meaning of enum symbols
	LPAREN("("), RPAREN(")"), PLUS("+"), MINUS("-"), MULTI("*"),	 DIVIDE("/"),
	AND("&"), OR("|"), NUMBER(""), NOT("!"),
	EQUAL("=="), LT("<"), LTEQ("<="), GT(">"), GTEQ(">="), NOTEQ("!="), NLINE("\n");
*/
public class Calculator
{

    Relop token; int value; int ch; int ch2; // ch는 연산 구분,ch2는 ==,<=,>= 구분을 위해 사용됨
    // 터미널 심볼 => token
    boolean bool; // bool
    private PushbackInputStream input; // 데이터의 읽기복구 기능을 제공하는 바이트 입력스트림
    final int TRUE = 1;
    final int FALSE = 0;

    Calculator(PushbackInputStream is) {
        input = is;
    }

    Relop getToken( ) // 연산자 구분
    {
        while(true)
        {
            try
            {
                ch = input.read();
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else if (Character.isDigit(ch))
                {
                    value = number( ); //숫자로 바꿔서 value에 저장
                    input.unread(ch); // 1바이트를 읽기 전 상태로 되돌림
                    return Relop.NUMBER;
                } // if문을 통해 연산자 구분
                else if (ch == '=') //비교연산자 구분
                {
                    ch2 = input.read();
                    if (ch2 == '=')
                        return Relop.EQUAL;
                    else error();
                }
                else if (ch == '>')
                {
                    ch2 = input.read();
                    if (ch2 == '=')
                        return Relop.GTEQ;
                    else
                    {
                        input.unread(ch2);
                        return Relop.GT;
                    }
                }
                else if (ch == '<')
                {
                    ch2 = input.read();
                    if (ch2 == '=')
                        return Relop.LTEQ;
                    else
                    {
                        input.unread(ch2);
                        return Relop.LT;
                    }
                }
                else if (ch == '!')
                {
                    ch2 = input.read();
                    if (ch2 == '=')
                        return Relop.NOTEQ;
                    else
                        return Relop.NOT;
                }
                else if (ch == '&') // &
                    return Relop.AND;
                else if (ch == '|')// |
                    return Relop.OR;
                else if (ch == '+')
                    return Relop.PLUS; // +
                else if (ch == '-')
                    return Relop.MINUS; // -
                else if (ch == '*')
                    return Relop.MULTI; // *
                else if (ch == '/')
                    return Relop.DIVIDE; // /
                else if (ch == '(')
                    return Relop.LPAREN; // (
                else if (ch == ')')
                    return Relop.RPAREN; // )
                else if (ch == '\n')
                    return Relop.NLINE;		// \n
            }
            catch (IOException e)
            {
                System.err.println(e);
            }
        }
    }

    void match(Relop c)
    {
        if (token == c)
            token = getToken();
        else error();
    }

    int expr() // 논리연산
    {
        //<expr> → <bexp> {& <bexp> | ‘|’ <bexp>} | !<expr> | true | false
        int result = bexp();
        while (token == Relop.AND || token == Relop.OR) //연산자가 & or |일 경우
        {
            if (token == Relop.AND)
            {
                match(Relop.AND);
                result = (result == TRUE && bexp() == TRUE) ? TRUE : FALSE; // 삼항 연산자로 bool값 연산
            }
            else
            {
                match(Relop.OR);
                result = (result == TRUE || bexp() == TRUE) ? TRUE : FALSE; // 삼항 연산자로 bool값 연산
            }
        }
        return result;
    }

    int bexp() // 비교연산
    {
        //<aexp> [(== | != | < | > | <= | >=) <aexp>]
        int result = aexp();
        if (token == Relop.EQUAL || token == Relop.NOTEQ ||
                token == Relop.LT || token == Relop.GT ||
                token == Relop.LTEQ || token == Relop.GTEQ) //연산자가 비교연산자일 경우
        {
            Relop op = token;
            match(token);
            int right = aexp();
            switch (op) { //switch 문을 통해 각 조건문에 들어갈 수 있도록 설정 및 삼항 연산자로 bool값 연산
                case EQUAL:
                    result = (result == right) ? TRUE : FALSE;
                    break;
                case NOTEQ:
                    result = (result != right) ? TRUE : FALSE;
                    break;
                case LT:
                    result = (result < right) ? TRUE : FALSE;
                    break;
                case GT:
                    result = (result > right) ? TRUE : FALSE;
                    break;
                case LTEQ:
                    result = (result <= right) ? TRUE : FALSE;
                    break;
                case GTEQ:
                    result = (result >= right) ? TRUE : FALSE;
                    break;
            }
        }
        return result;
    }

    int aexp() // 계산 +,-
    {
        //<aexp> → <term> {+ <term> | - <term>}
        int result = term();
        while (token == Relop.PLUS || token == Relop.MINUS) //+,-인지 구분하여 계산 실행
        {
            if (token == Relop.PLUS)
            {
                match(Relop.PLUS);
                result += term();
            }
            else
            {
                match(Relop.MINUS);
                result -= term();
            }
        }
        return result;
    }

    int term() // 계산 *,/
    {
        //<term> → <factor> {* <factor> | / <factor>}
        int result = factor();
        while (token == Relop.MULTI || token == Relop.DIVIDE) // *,/인지 구분하여 계산 실행
        {
            if (token == Relop.MULTI)
            {
                match(Relop.MULTI);
                result *= factor();
            }
            else
            {
                match(Relop.DIVIDE);
                int divisor = factor();
                if (divisor != 0) //0으로 나눌수 없으므로 조건문을 추가해준다.
                {
                    result /= divisor;
                }
                else
                {
                    System.out.println("0 is NOT"); //에러문을 출력해준다.
                    System.exit(1);
                }
            }
        }
        return result;
    }

    int factor()
    {
        // <number> | ‘(’<aexp>‘)’
        int result = 0;
        if (token == Relop.LPAREN) // (을 구분
        {
            match(Relop.LPAREN);
            result = expr();
            match(Relop.RPAREN);
        }
        else if (token == Relop.NUMBER) // 숫자를 구분
        {
            result = value;
            match(Relop.NUMBER);
        }

        else if (token == Relop.NOT) // !을 구분
        {
            match(Relop.NOT);
            result = expr();
            result = (result == TRUE) ? FALSE : TRUE;
        }

        return result;
    }


    int number( )
    {
        /* number -> digit { digit } */
        int result = ch - '0'; //입력값은 문자이므로 문자 0을 빼서 숫자로 바꿈
        try
        {
            ch = input.read();
            while (Character.isDigit(ch))
            {
                result = 10 * result + ch -'0'; //10을 곱하여 두자리 이상 수를 만들어줌
                ch = input.read();
            }
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
        return result;
    }

    void error( )
    {
        System.out.printf("parse error : %d\n", ch);
        System.exit(1);
    }

    void command( )
    {
        /* command -> expr '\n' */
        int result = expr();
        if (token == Relop.NLINE)
            if (result == TRUE)
                System.out.println(true);
            else if(result == FALSE)
                System.out.println(false);
            else
                System.out.printf("The result is: %d\n", result);
        else error();
    }

    void parse( )
    {
        token = getToken();
        command();
    }

    public static void main(String args[])
    {
        Calculator calc = new Calculator(new PushbackInputStream(System.in));
        while(true)
        {
            System.out.print(">> ");
            calc.parse();
        }
    }
}