# Git Related

## Link Repository

```
git remote add origin https://github.com/YMing686/ToyProject.git
git branch -M main
git add .
git commit -m "Initial commit"
git push -u origin main

# For submodules
rm -rf {submodule_name}/.git
rm -f .gitmodules
git add {submodule_name}
# Then Git ADD, COMMIT, and PUSH   
```
