#!/usr/bin/env python3
import subprocess
import os
import sys
import shutil

def compile_typst(typst_file, output_pdf):
    print(f"Compiling {os.path.basename(typst_file)} -> {os.path.basename(output_pdf)}...")
    cmd = ["/opt/homebrew/bin/typst", "compile", typst_file, output_pdf]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode == 0:
        file_size = os.path.getsize(output_pdf)
        print(f"  SUCCESS: Generated {output_pdf} ({file_size} bytes)")
    else:
        print(f"  ERROR compiling {typst_file}:", res.stderr)
        sys.exit(1)

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    pdf_dir = os.path.join(project_root, "docs", "pdf")
    os.makedirs(pdf_dir, exist_ok=True)
    
    en_typ = os.path.join(project_root, "docs", "book.typ")
    tr_typ = os.path.join(project_root, "docs", "book_tr.typ")
    
    en_pdf = os.path.join(pdf_dir, "Google_Senior_SWE_Master_Guide_EN.pdf")
    tr_pdf = os.path.join(pdf_dir, "Google_Senior_SWE_Master_Guide_TR.pdf")
    main_pdf = os.path.join(pdf_dir, "Google_Senior_SWE_Master_Guide.pdf")

    compile_typst(en_typ, en_pdf)
    compile_typst(tr_typ, tr_pdf)
    
    # Copy EN as default main PDF
    shutil.copy2(en_pdf, main_pdf)
    print("All English and Turkish PDFs generated successfully.")

if __name__ == "__main__":
    main()
