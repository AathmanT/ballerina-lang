/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangUnLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * Closure desugar for closure related scenarios.
 *
 * @since 0.990.5
 */
public class ClosureDesugar extends BLangNodeVisitor {
    private static final CompilerContext.Key<ClosureDesugar> CLOSURE_DESUGAR_KEY = new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolEnv env;
    private BLangNode result;
    private Types types;
    private Desugar desugar;
    private Names names;
    private int funClosureMapCount = 1;
    private int blockClosureMapCount = 1;

    public static ClosureDesugar getInstance(CompilerContext context) {
        ClosureDesugar desugar = context.get(CLOSURE_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new ClosureDesugar(context);
        }

        return desugar;
    }

    private ClosureDesugar(CompilerContext context) {
        context.put(CLOSURE_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.names = Names.getInstance(context);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        pkgNode.topLevelNodes.stream().filter(pkgLevelNode -> !(pkgLevelNode.getKind() == NodeKind.FUNCTION
                && ((BLangFunction) pkgLevelNode).flagSet.contains(Flag.LAMBDA))).forEach(
                topLevelNode -> rewrite((BLangNode) topLevelNode, pkgEnv));

        // Reverse the lambdas since in Desugar they are visited from inner to outer lambdas.
        List<BLangLambdaFunction> lambdasCollected = new ArrayList<>(pkgNode.lambdaFunctions);
        Collections.reverse(lambdasCollected);
        pkgNode.lambdaFunctions = new LinkedList<>(lambdasCollected);

        while (pkgNode.lambdaFunctions.peek() != null) {
            BLangLambdaFunction lambdaFunction = pkgNode.lambdaFunctions.poll();
            lambdaFunction.function = rewrite(lambdaFunction.function, lambdaFunction.cachedEnv);
        }

        // Update function parameters.
        pkgNode.functions.forEach(this::updateFunctionParams);

        result = pkgNode;
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.originalFuncSymbol.scope, env);
        funClosureMapCount++;

        // Check if function parameters are exposed as parameters.
        Optional<BVarSymbol> paramsExposed = funcNode.symbol.params.stream().filter(bVarSymbol -> bVarSymbol.closure)
                .findAny();
        int position = 1;
        if (paramsExposed.isPresent()) {
            createFunctionMap(funcNode, funcEnv);
            // Add the parameters of the functions that are exposed as closures to the function map.
            for (BVarSymbol paramSymbol : funcNode.symbol.params) {
                if (!paramSymbol.closure) {
                    continue;
                }
                addToFunctionMap(funcNode, funcEnv, position, paramSymbol, paramSymbol.type);
                position++;
            }
        }

        // Check if the rest param is a closure var
        if (funcNode.symbol.restParam != null && funcNode.symbol.restParam.closure) {
            if (funcNode.mapSymbol == null) {
                createFunctionMap(funcNode, funcEnv);
            }
            addToFunctionMap(funcNode, funcEnv, position, funcNode.symbol.restParam,
                             funcNode.symbol.restParam.type);
            position++;
        }

        // For attached functions add the receiver to the function map if it has been exposed as a closure.
        BLangSimpleVariable receiver = funcNode.receiver;
        if (receiver != null && receiver.symbol.closure && funcNode.flagSet.contains(Flag.ATTACHED)) {
            if (funcNode.mapSymbol == null) {
                createFunctionMap(funcNode, funcEnv);
            }
            addToFunctionMap(funcNode, funcEnv, position, receiver.symbol, receiver.type);
        }

        funcNode.body = rewrite(funcNode.body, funcEnv);
        result = funcNode;
    }

    /**
     * Create a function map to add parameters of the function that are used as closures.
     *
     * @param funcNode function node
     * @param funcEnv  function environment
     */
    private void createFunctionMap(BLangFunction funcNode, SymbolEnv funcEnv) {
        funcNode.mapSymbol = createMapSymbol("$map$func$" + funClosureMapCount, funcEnv);
        BLangRecordLiteral emptyRecord = ASTBuilderUtil.createEmptyRecordLiteral(funcNode.pos, symTable.mapType);
        BLangSimpleVariable mapVar = ASTBuilderUtil.createVariable(funcNode.pos, funcNode.mapSymbol.name.value,
                funcNode.mapSymbol.type, emptyRecord, funcNode.mapSymbol);
        mapVar.typeNode = ASTBuilderUtil.createTypeNode(funcNode.mapSymbol.type);
        BLangSimpleVariableDef mapVarDef = ASTBuilderUtil.createVariableDef(funcNode.pos, mapVar);
        // Add the map variable to the top of the statements in the block node.
        mapVarDef = desugar.rewrite(mapVarDef, funcEnv);
        // Add the map variable to the top of the statements in the block node.
        if (funcNode.body == null) {
            funcNode.body = ASTBuilderUtil.createBlockStmt(funcNode.pos);
        }
        funcNode.body.stmts.add(0, mapVarDef);
    }

    /**
     * Update the function parameters with closure parameter maps passed.
     *
     * @param funcNode function node
     */
    private void updateFunctionParams(BLangFunction funcNode) {
        // Add closure params to the required param list if there are any.
        BInvokableSymbol dupFuncSymbol = ASTBuilderUtil.duplicateInvokableSymbol(funcNode.symbol);
        funcNode.symbol = dupFuncSymbol;
        BInvokableType dupFuncType = (BInvokableType) dupFuncSymbol.type;

        int i = 0;
        for (Map.Entry<Integer, BVarSymbol> entry : funcNode.paramClosureMap.entrySet()) {
            BVarSymbol mapSymbol = entry.getValue();
            dupFuncSymbol.params.add(i, mapSymbol);
            dupFuncType.paramTypes.add(i, mapSymbol.type);
            i++;
        }
    }

    /**
     * Add function parameters exposed as closures to the function map.
     *
     * @param funcNode    function node
     * @param symbolEnv   symbol environment
     * @param position    position to be added
     * @param paramSymbol parameter symbol
     * @param type        parameter type
     */
    private void addToFunctionMap(BLangFunction funcNode, SymbolEnv symbolEnv, int position, BVarSymbol paramSymbol,
                                  BType type) {
        BLangSimpleVarRef.BLangLocalVarRef localVarRef = new BLangSimpleVarRef.BLangLocalVarRef(paramSymbol);
        // Added the flag so it will not be desugared again.
        localVarRef.closureDesugared = true;
        localVarRef.type = type;
        BLangIndexBasedAccess accessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(funcNode.pos, type,
                funcNode.mapSymbol, ASTBuilderUtil.createLiteral(funcNode.pos, symTable.stringType,
                        paramSymbol.name.value));
        accessExpr.type = ((BMapType) funcNode.mapSymbol.type).constraint;
        accessExpr.lhsVar = true;
        BLangAssignment stmt = desugar.rewrite(ASTBuilderUtil.createAssignmentStmt(funcNode.pos, accessExpr,
                localVarRef), symbolEnv);
        funcNode.body.stmts.add(position, stmt);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockClosureMapCount++;
        blockNode.stmts = rewriteStmt(blockNode.stmts, blockEnv);

        // Add block map to the 0th position if a block map symbol is there.
        if (blockNode.mapSymbol != null) {
            BLangRecordLiteral emptyRecord =
                    ASTBuilderUtil.createEmptyRecordLiteral(blockNode.pos, blockNode.mapSymbol.type);
            BLangSimpleVariable mapVar = ASTBuilderUtil.createVariable(blockNode.pos,
                    blockNode.mapSymbol.name.value, blockNode.mapSymbol.type, emptyRecord,
                    blockNode.mapSymbol);
            mapVar.typeNode = ASTBuilderUtil.createTypeNode(blockNode.mapSymbol.type);
            BLangSimpleVariableDef mapVarDef = ASTBuilderUtil.createVariableDef(blockNode.pos, mapVar);
            // Add the map variable to the top of the statements in the block node.
            mapVarDef = desugar.rewrite(mapVarDef, blockEnv);
            blockNode.stmts.add(0, mapVarDef);
        }

        result = blockNode;
    }

    @Override
    public void visit(BLangService serviceNode) {
        /* Ignore */
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        if (!varDefNode.var.symbol.closure) {
            varDefNode.var = rewrite(varDefNode.var, env);
            result = varDefNode;
            return;
        }

        // If its a variable declaration with a RHS value, and also a closure.
        if (varDefNode.var.expr != null) {
            BLangAssignment stmt = createAssignment(varDefNode);
            result = rewrite(stmt, env);
        } else {
            result = varDefNode;
        }
    }

    /**
     * Replace the variable definition statement with as assignment statement for closures.
     *
     * @param varDefNode variable definition node
     * @return assignment statement created
     */
    private BLangAssignment createAssignment(BLangSimpleVariableDef varDefNode) {
        // Create a map for the function or block node.
        BLangBlockStmt blockStmt = (BLangBlockStmt) env.node;
        if (blockStmt.mapSymbol == null) {
            // create a map symbol
            blockStmt.mapSymbol = createMapSymbol("$map$block$" + blockClosureMapCount, env);
        }

        // Add the variable to the created map.
        BLangIndexBasedAccess accessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(varDefNode.pos, varDefNode.type,
                blockStmt.mapSymbol, ASTBuilderUtil.createLiteral(varDefNode.pos, symTable.stringType,
                        varDefNode.var.name.value));
        accessExpr.type = ((BMapType) blockStmt.mapSymbol.type).constraint;
        accessExpr.lhsVar = true;
        // Written to: 'map["x"] = 8'.
        return ASTBuilderUtil.createAssignmentStmt(varDefNode.pos, accessExpr, varDefNode.var.expr);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.expr != null) {
            returnNode.expr = rewriteExpr(returnNode.expr);
        }
        result = returnNode;
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        iExpr.expr = rewriteExpr(iExpr.expr);
        if (iExpr.requiredArgs.size() > 0) {
            iExpr.requiredArgs.set(0, iExpr.expr);
        }
        iExpr.requiredArgs = rewriteExprs(iExpr.requiredArgs);
        iExpr.restArgs = rewriteExprs(iExpr.restArgs);
        result = iExpr;
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        result = importPkgNode;
    }

    @Override
    public void visit(BLangTypeDefinition typeDef) {
        if (typeDef.typeNode.getKind() == NodeKind.OBJECT_TYPE
                || typeDef.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            typeDef.typeNode = rewrite(typeDef.typeNode, env);
        }
        result = typeDef;
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        result = objectTypeNode;
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        result = recordTypeNode;
    }

    public void visit(BLangForever foreverStatement) {
        result = rewrite(foreverStatement, env);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangTupleVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangRecordVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangErrorVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangTupleVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangRecordVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangErrorVariableDef varDefNode) {
        varDefNode.errorVariable = rewrite(varDefNode.errorVariable, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.varRef = rewriteExpr(assignNode.varRef);
        if (assignNode.expr.impConversionExpr != null) {
            types.setImplicitCastExpr(assignNode.expr.impConversionExpr, assignNode.expr.impConversionExpr.type,
                    assignNode.varRef.type);
        } else {
            types.setImplicitCastExpr(assignNode.expr, assignNode.expr.type, assignNode.varRef.type);
        }
        assignNode.expr = rewriteExpr(assignNode.expr);
        result = assignNode;
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructure) {
        result = tupleDestructure;
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {
        result = recordDestructure;
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure) {
        result = errorDestructure;
    }

    @Override
    public void visit(BLangAbort abortNode) {
        result = abortNode;
    }

    @Override
    public void visit(BLangRetry retryNode) {
        result = retryNode;
    }

    @Override
    public void visit(BLangContinue nextNode) {
        result = nextNode;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        result = breakNode;
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr = rewriteExpr(panicNode.expr);
        result = panicNode;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl = rewrite(xmlnsStmtNode.xmlnsDecl, env);
        result = xmlnsStmtNode;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewriteExpr(exprStmtNode.expr);
        result = exprStmtNode;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewriteExpr(ifNode.expr);
        ifNode.body = rewrite(ifNode.body, env);
        ifNode.elseStmt = rewrite(ifNode.elseStmt, env);
        result = ifNode;
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        result = matchStmt;
    }

    @Override
    public void visit(BLangForeach foreach) {
        result = foreach;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewriteExpr(whileNode.expr);
        whileNode.body = rewrite(whileNode.body, env);
        result = whileNode;
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.body = rewrite(lockNode.body, env);
        result = lockNode;
    }

    @Override
    public void visit(BLangLockStmt lockNode) {
        result = lockNode;
    }

    @Override
    public void visit(BLangUnLockStmt unLockNode) {
        result = unLockNode;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody, env);
        transactionNode.onRetryBody = rewrite(transactionNode.onRetryBody, env);
        transactionNode.committedBody = rewrite(transactionNode.committedBody, env);
        transactionNode.abortedBody = rewrite(transactionNode.abortedBody, env);
        transactionNode.retryCount = rewriteExpr(transactionNode.retryCount);
        result = transactionNode;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        result = forkJoin;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs = rewriteExprs(listConstructorExpr.exprs);
        result = listConstructorExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        jsonArrayLiteral.exprs = rewriteExprs(jsonArrayLiteral.exprs);
        result = jsonArrayLiteral;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        tupleLiteral.exprs = rewriteExprs(tupleLiteral.exprs);
        result = tupleLiteral;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs = rewriteExprs(arrayLiteral.exprs);
        result = arrayLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // Process the key-val pairs in the record literal.
        recordLiteral.keyValuePairs.forEach(keyValue -> {
            keyValue.key.expr = rewriteExpr(keyValue.key.expr);
            keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
        });
        result = recordLiteral;
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        tableLiteral.tableDataRows = rewriteExprs(tableLiteral.tableDataRows);
        tableLiteral.indexColumnsArrayLiteral = rewriteExpr(tableLiteral.indexColumnsArrayLiteral);
        tableLiteral.keyColumnsArrayLiteral = rewriteExpr(tableLiteral.keyColumnsArrayLiteral);
        result = tableLiteral;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr = rewriteExpr(fieldAccessExpr.expr);
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        result = desugar.rewriteExpr(indexAccessExpr);
    }

    @Override
    public void visit(BLangInvocation iExpr) {
        iExpr.expr = rewriteExpr(iExpr.expr);
        iExpr.requiredArgs = rewriteExprs(iExpr.requiredArgs);
        iExpr.restArgs = rewriteExprs(iExpr.restArgs);
        result = iExpr;
    }

    public void visit(BLangTypeInit typeInitExpr) {
        typeInitExpr.argsExpr.forEach(argExpr -> rewrite(argExpr, env));
        typeInitExpr.initInvocation = rewriteExpr(typeInitExpr.initInvocation);
        result = typeInitExpr;
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr = rewriteExpr(ternaryExpr.expr);
        ternaryExpr.thenExpr = rewriteExpr(ternaryExpr.thenExpr);
        ternaryExpr.elseExpr = rewriteExpr(ternaryExpr.elseExpr);
        result = ternaryExpr;
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        List<BLangExpression> exprList = new ArrayList<>();
        waitExpr.exprList.forEach(expression -> exprList.add(rewriteExpr(expression)));
        waitExpr.exprList = exprList;
        result = waitExpr;
    }

    @Override
    public void visit(BLangWaitForAllExpr waitExpr) {
        result = waitExpr;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr = rewriteExpr(trapExpr.expr);
        result = trapExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr = rewriteExpr(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = rewriteExpr(binaryExpr.rhsExpr);
        result = binaryExpr;
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        result = elvisExpr;
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression = rewriteExpr(groupExpr.expression);
        result = groupExpr;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = rewriteExpr(unaryExpr.expr);
        result = unaryExpr;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // If there is an implicit cast on the expr of the type conversion expr already we do not rewrite it. This is
        // to avoid stackoverflow error.
        if (conversionExpr.expr.impConversionExpr != null) {
            result = conversionExpr;
            return;
        }
        conversionExpr.expr = rewriteExpr(conversionExpr.expr);
        result = conversionExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.cachedEnv = env.createClone();
        bLangLambdaFunction.enclMapSymbols = collectClosureMapSymbols(bLangLambdaFunction.cachedEnv,
                bLangLambdaFunction);
        result = bLangLambdaFunction;
    }

    private TreeMap<Integer, BVarSymbol> collectClosureMapSymbols(SymbolEnv symbolEnv,
                                                                  BLangLambdaFunction bLangLambdaFunction) {
        // Save param closure map of the encl invokable.
        bLangLambdaFunction.paramMapSymbolsOfEnclInvokable = ((BLangFunction) symbolEnv.enclInvokable).paramClosureMap;

        // Recursively iterate back to the encl invokable and get all map symbols visited.
        TreeMap<Integer, BVarSymbol> enclMapSymbols = new TreeMap<>();
        while (symbolEnv != null && symbolEnv.enclInvokable == bLangLambdaFunction.cachedEnv.enclInvokable) {
            if (symbolEnv.node.getKind() == NodeKind.FUNCTION) {
                if (((BLangFunction) symbolEnv.node).mapSymbol != null) {
                    enclMapSymbols.putIfAbsent(symbolEnv.envCount, ((BLangFunction) symbolEnv.node).mapSymbol);
                }
            } else if (symbolEnv.node.getKind() == NodeKind.BLOCK) {
                if (((BLangBlockStmt) symbolEnv.node).mapSymbol != null) {
                    enclMapSymbols.putIfAbsent(symbolEnv.envCount, ((BLangBlockStmt) symbolEnv.node).mapSymbol);
                }
            }
            symbolEnv = symbolEnv.enclEnv;
        }
        return enclMapSymbols;
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        result = bLangArrowFunction;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        result = xmlQName;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.name = rewriteExpr(xmlAttribute.name);
        xmlAttribute.value = rewriteExpr(xmlAttribute.value);
        result = xmlAttribute;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.startTagName = rewriteExpr(xmlElementLiteral.startTagName);
        xmlElementLiteral.endTagName = rewriteExpr(xmlElementLiteral.endTagName);
        xmlElementLiteral.modifiedChildren = rewriteExprs(xmlElementLiteral.modifiedChildren);
        xmlElementLiteral.attributes = rewriteExprs(xmlElementLiteral.attributes);
        result = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.textFragments.forEach(this::rewriteExpr);
        xmlTextLiteral.concatExpr = rewriteExpr(xmlTextLiteral.concatExpr);
        result = xmlTextLiteral;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.textFragments.forEach(this::rewriteExpr);
        result = xmlCommentLiteral;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target = rewriteExpr(xmlProcInsLiteral.target);
        xmlProcInsLiteral.dataFragments.forEach(this::rewriteExpr);
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.textFragments.forEach(this::rewriteExpr);
        result = xmlQuotedString;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(this::rewriteExpr);
        result = stringTemplateLiteral;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr = rewriteExpr(workerSendNode.expr);
        if (workerSendNode.keyExpr != null) {
            workerSendNode.keyExpr = rewriteExpr(workerSendNode.keyExpr);
        }
        result = workerSendNode;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.expr = rewriteExpr(syncSendExpr.expr);
        result = syncSendExpr;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        if (workerReceiveNode.keyExpr != null) {
            workerReceiveNode.keyExpr = rewriteExpr(workerReceiveNode.keyExpr);
        }
        result = workerReceiveNode;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        result = workerFlushExpr;
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.indexExpr = rewriteExpr(xmlAttributeAccessExpr.indexExpr);
        xmlAttributeAccessExpr.expr = rewriteExpr(xmlAttributeAccessExpr.expr);
        result = xmlAttributeAccessExpr;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // Chek
        if (!localVarRef.symbol.closure || localVarRef.closureDesugared) {
            result = localVarRef;
            return;
        }

        // If it is marked as a closure variable then the following calculations are carried out.
        // 1) Find the resolved level i.e. the absolute level : level the variable was resolved from.
        int absoluteLevel = findResolvedLevel(env, (BVarSymbol) localVarRef.varSymbol);

        // self absolute level : level I'm currently in.
        int selfAbsoluteLevel = env.envCount;

        // self relative count : block nodes above me in the same function.
        int selfRelativeCount = env.relativeEnvCount;

        // 2) Update the variable reference.

        // selfRelativeCount >= selfAbsoluteLevel - absoluteLevel ==> resolved within the same function.
        if (selfRelativeCount >= selfAbsoluteLevel - absoluteLevel) {

            // Go up within the block node
            SymbolEnv symbolEnv = env;
            while (symbolEnv != null && symbolEnv.node.getKind() != NodeKind.PACKAGE) {
                // Check if the node is a block statement.
                if (symbolEnv.envCount == absoluteLevel && symbolEnv.node.getKind() == NodeKind.BLOCK) {
                    if (((BLangBlockStmt) symbolEnv.node).mapSymbol == null) {
                        ((BLangBlockStmt) symbolEnv.node).mapSymbol =
                                createMapSymbol("$map$block$" + symbolEnv.envCount, env);
                    }
                    updateClosureVars(localVarRef, ((BLangBlockStmt) symbolEnv.node).mapSymbol);
                    return;
                }
                // Check if the node is a function.
                if (symbolEnv.envCount == absoluteLevel && symbolEnv.node.getKind() == NodeKind.FUNCTION) {
                    if (((BLangFunction) symbolEnv.node).mapSymbol == null) {
                        ((BLangFunction) symbolEnv.node).mapSymbol =
                                createMapSymbol("$map$func$" + symbolEnv.envCount, env);
                    }
                    updateClosureVars(localVarRef, ((BLangFunction) symbolEnv.node).mapSymbol);
                    return;
                }

                symbolEnv = symbolEnv.enclEnv;
            }
        } else {
            // It is resolved from a parameter map.
            // Add parameter map symbol if one is not added.
            ((BLangFunction) env.enclInvokable).paramClosureMap.putIfAbsent(absoluteLevel, createMapSymbol(
                    "$paramMap$" + absoluteLevel, env));

            // Update the closure vars.
            updateClosureVars(localVarRef, ((BLangFunction) env.enclInvokable).paramClosureMap.get(absoluteLevel));

        }

        // 3) Add the resolved level of the closure variable to the preceding function maps.
        updatePrecedingFunc(env, absoluteLevel);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        result = ignoreExpr;
    }

    /**
     * Find the resolved level of the closure variable.
     *
     * @param symbolEnv symbol environment
     * @param varSymbol symbol of the closure
     * @return level it was resolved
     */
    private int findResolvedLevel(SymbolEnv symbolEnv, BVarSymbol varSymbol) {
        while (symbolEnv != null && symbolEnv.node.getKind() != NodeKind.PACKAGE) {
            Scope.ScopeEntry entry = symbolEnv.scope.lookup(varSymbol.name);
            if (entry != NOT_FOUND_ENTRY && varSymbol == entry.symbol && varSymbol.owner == symbolEnv.scope.owner) {
                return symbolEnv.envCount;
            }
            symbolEnv = symbolEnv.enclEnv;
        }
        // 0 is returned if it was not found.
        return 0;
    }

    /**
     * Update the preceeding functions.
     *
     * @param symbolEnv     symbol environment
     * @param resolvedLevel resolved level of the closure variable
     */
    private void updatePrecedingFunc(SymbolEnv symbolEnv, int resolvedLevel) {
        while (symbolEnv != null && symbolEnv.node.getKind() != NodeKind.PACKAGE) {
            // If the symbol env count equals to the resolved level then return.
            if (symbolEnv.envCount == resolvedLevel) {
                return;
            }

            // If the node is not a function, then get its enclosing env and continue.
            if (symbolEnv.node.getKind() != NodeKind.FUNCTION) {
                symbolEnv = symbolEnv.enclEnv;
                continue;
            }

            // If the node is a function, update it accordingly.
            BLangFunction bLangFunction = (BLangFunction) symbolEnv.node;
            if (symbolEnv.enclInvokable == env.enclInvokable) {
                symbolEnv = symbolEnv.enclEnv;
                continue;
            }
            if (bLangFunction.paramClosureMap.containsKey(resolvedLevel)) {
                return;
            }
            bLangFunction.paramClosureMap.put(resolvedLevel, createMapSymbol("$paramMap$" + resolvedLevel,
                    symbolEnv));

            symbolEnv = symbolEnv.enclEnv;
        }
    }

    /**
     * Create the map symbol required for the function node and block statements.
     *
     * @param mapName   name of the map to be created
     * @param symbolEnv symbol environment
     * @return map symbol created
     */
    private BVarSymbol createMapSymbol(String mapName, SymbolEnv symbolEnv) {
        BUnionType unionType = BUnionType.create(null, symTable.anyType, symTable.errorType);
        return new BVarSymbol(0, names.fromString(mapName), symbolEnv.scope.owner.pkgID,
                new BMapType(TypeTags.MAP, unionType, null), symbolEnv.scope.owner);
    }

    /**
     * Update the closure maps with the relevant map access expression.
     *
     * @param varRefExpr closure variable reference to be updated
     * @param mapSymbol  map symbol to be used
     */
    private void updateClosureVars(BLangSimpleVarRef varRefExpr, BVarSymbol mapSymbol) {
        // Get type of the index based access expression.
        BType typeOfExpr = getTypeOfIndexBasedAccessExpr(varRefExpr.type);
        // Create the index based access expression.
        BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(varRefExpr.pos, symTable.stringType,
                varRefExpr.varSymbol.name.value);
        BLangIndexBasedAccess accessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(varRefExpr.pos, typeOfExpr,
                mapSymbol, indexExpr);
        // If its in the LHS of an assignment.
        if (varRefExpr.lhsVar) {
            // x = 1 ==> $innerMap$1["x"] = <any> 1
            result = rewriteExpr(accessExpr);
            return;
        }
        // int z = x + 1 ==> int z = <int>$innerMap$1["x"] + 1;
        result = rewriteExpr(desugar.addConversionExprIfRequired(accessExpr, varRefExpr.type));
    }

    /**
     * Get type of the index based access expression.
     *
     * @param bType type of the closure variable
     * @return type of the index based access expression
     */
    private BType getTypeOfIndexBasedAccessExpr(BType bType) {
        switch (bType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.JSON:
            case TypeTags.MAP:
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
            case TypeTags.TUPLE:
            case TypeTags.XML:
                return bType;
            default:
                // If its any other type return the any|error.
                return BUnionType.create(null, symTable.anyType, symTable.errorType);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        result = fieldVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        result = packageVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {
        result = constRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        fieldAccessExpr.indexExpr = rewriteExpr(fieldAccessExpr.indexExpr);
        fieldAccessExpr.expr = rewriteExpr(fieldAccessExpr.expr);
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        functionVarRef.expr = rewriteExpr(functionVarRef.expr);
        result = functionVarRef;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        mapKeyAccessExpr.indexExpr = rewriteExpr(mapKeyAccessExpr.indexExpr);
        mapKeyAccessExpr.expr = rewriteExpr(mapKeyAccessExpr.expr);
        result = mapKeyAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.indexExpr = rewriteExpr(arrayIndexAccessExpr.indexExpr);
        arrayIndexAccessExpr.expr = rewriteExpr(arrayIndexAccessExpr.expr);
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.indexExpr = rewriteExpr(arrayIndexAccessExpr.indexExpr);
        arrayIndexAccessExpr.expr = rewriteExpr(arrayIndexAccessExpr.expr);
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        xmlIndexAccessExpr.indexExpr = rewriteExpr(xmlIndexAccessExpr.indexExpr);
        xmlIndexAccessExpr.expr = rewriteExpr(xmlIndexAccessExpr.expr);
        result = xmlIndexAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        jsonAccessExpr.indexExpr = rewriteExpr(jsonAccessExpr.indexExpr);
        jsonAccessExpr.expr = rewriteExpr(jsonAccessExpr.expr);
        result = jsonAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        stringAccessExpr.indexExpr = rewriteExpr(stringAccessExpr.indexExpr);
        stringAccessExpr.expr = rewriteExpr(stringAccessExpr.expr);
        result = stringAccessExpr;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        jsonLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> {
            bLangRecordKeyValue.key.expr = rewriteExpr(bLangRecordKeyValue.key.expr);
            bLangRecordKeyValue.valueExpr = rewriteExpr(bLangRecordKeyValue.valueExpr);
        });
        result = jsonLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        mapLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> {
            bLangRecordKeyValue.key.expr = rewriteExpr(bLangRecordKeyValue.key.expr);
            bLangRecordKeyValue.valueExpr = rewriteExpr(bLangRecordKeyValue.valueExpr);
        });
        result = mapLiteral;
    }

    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        streamLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> {
            bLangRecordKeyValue.key.expr = rewriteExpr(bLangRecordKeyValue.key.expr);
            bLangRecordKeyValue.valueExpr = rewriteExpr(bLangRecordKeyValue.valueExpr);
        });
        result = streamLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        structLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> {
            bLangRecordKeyValue.key.expr = rewriteExpr(bLangRecordKeyValue.key.expr);
            bLangRecordKeyValue.valueExpr = rewriteExpr(bLangRecordKeyValue.valueExpr);
        });
        result = structLiteral;
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        waitLiteral.keyValuePairs.forEach(keyValue -> {
            if (keyValue.valueExpr != null) {
                keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
            } else {
                keyValue.keyExpr = rewriteExpr(keyValue.keyExpr);
            }
        });
        result = waitLiteral;
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr = rewriteExpr(assignableExpr.lhsExpr);
        result = assignableExpr;
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation fpInvocation) {
        fpInvocation.expr = rewriteExpr(fpInvocation.expr);
        fpInvocation.requiredArgs = rewriteExprs(fpInvocation.requiredArgs);
        fpInvocation.restArgs = rewriteExprs(fpInvocation.restArgs);
        result = fpInvocation;
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        result = accessExpr;
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.startExpr = rewriteExpr(intRangeExpression.startExpr);
        intRangeExpression.endExpr = rewriteExpr(intRangeExpression.endExpr);
        result = intRangeExpression;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        result = rewriteExpr(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.expr = rewriteExpr(bLangNamedArgsExpression.expr);
        result = bLangNamedArgsExpression.expr;
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        result = bLangMatchExpression;
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        result = checkedExpr;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        result = serviceConstructorExpr;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr = rewriteExpr(typeTestExpr.expr);
        result = typeTestExpr;
    }

    @Override
    public void visit(BLangIsLikeExpr isLikeExpr) {
        isLikeExpr.expr = rewriteExpr(isLikeExpr.expr);
        result = isLikeExpr;
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        annotAccessExpr.expr = rewriteExpr(annotAccessExpr.expr);
        result = annotAccessExpr;
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.expr = rewriteExpr(bLangStatementExpression.expr);
        bLangStatementExpression.stmt = rewrite(bLangStatementExpression.stmt, env);
        result = bLangStatementExpression;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation aIExpr) {
        result = aIExpr;
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation iExpr) {
        iExpr.expr = rewriteExpr(iExpr.expr);
        iExpr.requiredArgs = rewriteExprs(iExpr.requiredArgs);
        iExpr.restArgs = rewriteExprs(iExpr.restArgs);
        result = iExpr;
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangConstant constant) {
        result = constant;
    }

    @Override
    public void visit(BLangWorker workerNode) {
        result = workerNode;
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        /* Ignore */
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        /* Ignore */
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        result = typeLoad;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        channelLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> {
            bLangRecordKeyValue.key.expr = rewriteExpr(bLangRecordKeyValue.key.expr);
            bLangRecordKeyValue.valueExpr = rewriteExpr(bLangRecordKeyValue.valueExpr);
        });
        result = channelLiteral;
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        result = bLangXMLSequenceLiteral;
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause langMatchStaticBindingPatternClause) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause matchStructuredBindingPatternClause) {
        /* Ignore */
    }

    // Rewrite methods
    @SuppressWarnings("unchecked")
    private <E extends BLangNode> E rewrite(E node, SymbolEnv env) {
        if (node == null) {
            return null;
        }

        SymbolEnv previousEnv = this.env;
        this.env = env;

        node.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;

        this.env = previousEnv;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangExpression> E rewriteExpr(E node) {
        if (node == null) {
            return null;
        }

        BLangExpression expr = node;
        if (node.impConversionExpr != null) {
            expr = node.impConversionExpr;
            node.impConversionExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangStatement> List<E> rewriteStmt(List<E> nodeList, SymbolEnv env) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i), env));
        }
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangExpression> List<E> rewriteExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewriteExpr(nodeList.get(i)));
        }
        return nodeList;
    }
}
