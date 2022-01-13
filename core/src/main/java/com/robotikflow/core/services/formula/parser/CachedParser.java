package com.robotikflow.core.services.formula.parser;

import java.io.StringReader;

import com.robotikflow.core.services.formula.eval.AstExpression;
import com.robotikflow.core.services.formula.scanner.Scanner;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CachedParser
{
    @Cacheable(value = "formulaParser")
    public AstExpression parse(
        final String script) 
        throws Exception
    {
        var scanner = new Scanner(new StringReader(script));
        return (AstExpression)new Parser(scanner).parse().value;
    }
}