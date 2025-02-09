import {
    Assignment, ASTNode, ASTUtil, Block, ExpressionStatement, Function as BalFunction,
    If, Return, VariableDef, VisibleEndpoint, Visitor, WorkerSend
} from "@ballerina/ast-model";
import { EndpointViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";
import { WorkerSendViewState } from "../view-model/worker-send";

let visibleEPsInCurrentFunc: VisibleEndpoint[] = [];
let envEndpoints: VisibleEndpoint[] = [];

function initStatement(node: ASTNode) {
    if (!node.viewState) {
        node.viewState = new StmntViewState();
    }
    // undo previous expandings
    // the same statement may be expanded inside some functions but not in others
    // (node.viewState as StmntViewState).expandContext = undefined;
}

export const visitor: Visitor = {

    beginVisitASTNode(node: ASTNode) {
        if (!node.viewState) {
            node.viewState = new ViewState();
        }
    },

    beginVisitBlock(node: Block, parent: ASTNode) {
        if (!node.viewState) {
            node.viewState = new BlockViewState();
        }
        node.parent = parent;
        if (!node.parent) {
            return;
        }
        const parentNode = (parent as (If | BalFunction));
        if (parentNode.VisibleEndpoints) {
            envEndpoints = [...envEndpoints, ...parentNode.VisibleEndpoints];
        }
    },

    endVisitBlock(node: Block, parent: ASTNode) {
        if (!node.parent) {
            return;
        }
        const parentNode = (parent as (If | BalFunction));
        if (parentNode.VisibleEndpoints) {
            const parentsVisibleEndpoints = parentNode.VisibleEndpoints;
            envEndpoints = envEndpoints.filter((ep) => (!parentsVisibleEndpoints.includes(ep)));
        }
    },

    beginVisitFunction(node: BalFunction) {
        if (node.VisibleEndpoints) {
            visibleEPsInCurrentFunc = [...node.VisibleEndpoints, ...visibleEPsInCurrentFunc];
        }
        if (!node.viewState) {
            const viewState = new FunctionViewState();
            node.viewState = viewState;
        }
        if (node.body) {
            node.body.statements.forEach((statement, index) => {
                // Hide All worker nodes.
                if (ASTUtil.isWorker(statement)) {
                    if (!statement.viewState) {
                        statement.viewState = new WorkerViewState();
                    }
                    statement.viewState.hidden = true;
                    const nextStmt = node.body!.statements[index + 1];
                    if (nextStmt) {
                        if (!nextStmt.viewState) {
                            nextStmt.viewState = new ViewState();
                        }
                        nextStmt.viewState.hidden = true;
                    }
                }
            });
        }
    },

    endVisitFunction(node: BalFunction) {
        const viewState = node.viewState as FunctionViewState;

        if (viewState.isExpandedFunction) {
            const toAdd: VisibleEndpoint[] = [];
            const added: any = {};
            visibleEPsInCurrentFunc.forEach((ep) => {
                if (!added[ep.name]) {
                    toAdd.push(ep);
                    added[ep.name] = true;
                }
            });
            viewState.containingVisibleEndpoints = toAdd;
        } else {
            visibleEPsInCurrentFunc = [];
        }
    },

    beginVisitCompilationUnit(node: ASTNode) {
        // view state will be set by the diagram component.
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        initStatement(node);

        if (ASTUtil.isActionInvocation(node)) {
            return;
        }
    },

    endVisitVariableDef(node: VariableDef) {
        initStatement(node);
    },

    endVisitAssignment(node: Assignment) {
        initStatement(node);
    },

    beginVisitVisibleEndpoint(node: VisibleEndpoint) {
        if (!node.viewState) {
            node.viewState = new EndpointViewState();
        }
        // show locally defined endpoints by default
        (node.viewState as EndpointViewState).visible = node.isLocal;
    },

    beginVisitReturn(node: Return) {
        if (!node.viewState) {
            node.viewState = new ReturnViewState();
        }
    },

    beginVisitWorkerSend(node: WorkerSend) {
        node.viewState = new WorkerSendViewState();
    },
};
