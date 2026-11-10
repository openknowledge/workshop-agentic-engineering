import type { Plugin } from "@opencode-ai/plugin";

const PROTECTED_FILES = ["pom.xml", "package.json", ".env", "opencode.json"];

export const GuardrailPlugin: Plugin = async () => {
  return {
    "tool.execute.before": async (input, output) => {
      // 1. Direkte File-Tools (edit / write) prüfen
      if (input.tool === "edit" || input.tool === "write") {
        const filePath: string = output.args?.filePath || output.args?.path || "";
        if (PROTECTED_FILES.some((f) => filePath.endsWith(f))) {
          throw new Error(
            `BLOCKED BY HOOK: Modifying '${filePath}' is strictly forbidden.`
          );
        }
      }

      // 2. Bash-Umgehungen abfangen (z. B. sed, python, echo > pom.xml)
      if (input.tool === "bash") {
        const cmd: string = output.args?.command || "";
        if (PROTECTED_FILES.some((f) => cmd.includes(f))) {
          throw new Error(
            `BLOCKED BY HOOK: Bash command references a protected file (${PROTECTED_FILES.join(", ")}).`
          );
        }
      }
    },
  };
};
