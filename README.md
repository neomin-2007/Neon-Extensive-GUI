# ⚡ Neon-Extensive-GUI

> Base de código para mods de UI em **Minecraft Forge 1.8.9** com sistema de configuração extensível, tipado e persistente — pronto para você plugar suas próprias opções sem tocar na lógica de renderização.

---

## 📋 Índice

- [O que é](#o-que-é)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Como adicionar uma opção](#como-adicionar-uma-opção)
    - [Boolean](#boolean-toggleonoff)
    - [Integer](#integer)
    - [Float](#float)
    - [String](#string)
    - [Character](#character-tecla-de-atalho)
    - [OptionalData (dropdown)](#optionaldata-dropdown)
- [Como criar uma categoria](#como-criar-uma-categoria)
- [Lendo e escrevendo valores em runtime](#lendo-e-escrevendo-valores-em-runtime)
- [Persistência](#persistência)
- [Abrindo a UI](#abrindo-a-ui)
- [Requisitos](#requisitos)

---

## O que é

Neon-Extensive-GUI é uma **base reutilizável** de sistema de configuração para mods de UI no Forge 1.8.9. Ela oferece:

- Tela de configuração com sidebar por categorias
- Suporte nativo a `Boolean`, `Integer`, `Float`, `String`, `Character` e `OptionalData` (dropdown)
- Persistência automática via arquivo `.cfg` do Forge
- Dropdown sobreposto para opções com múltiplas escolhas
- Scroll na lista de opções
- Zero boilerplate — basta declarar suas opções no enum

---

## Estrutura do projeto

```
neomin/uimod/
├── api/
│   ├── data/
│   │   ├── Option.java          # Wrapper genérico de valor
│   │   └── OptionalData.java    # Lista de opções para dropdown
│   └── enums/
│       ├── OptionCategoryType.java  # Categorias da sidebar
│       └── OptionType.java          # Todas as opções do mod
└── gameplay/
    ├── services/
    │   └── OptionsService.java  # Leitura, escrita e persistência
    └── ui/
        └── ConfigUI.java        # Tela de configuração
```

---

## Como adicionar uma opção

Todas as opções ficam no enum `OptionType`. Cada entrada recebe:

```java
NOME_DA_OPCAO("Label visível", OptionCategoryType.CATEGORIA, valorPadrão, Tipo.class)
```

### Boolean (toggle ON/OFF)

```java
MINHA_OPCAO_BOOL("Mostrar HUD", OptionCategoryType.GRAPHICAL, true, Boolean.class),
```

Renderiza como um toggle clicável. Valor padrão é `true` ou `false`.

---

### Integer

```java
MINHA_OPCAO_INT("Tamanho da fonte", OptionCategoryType.GENERAL, 12, Integer.class),
```

Renderiza como campo de texto numérico. Confirma com `Enter` ou ao clicar fora.

---

### Float

```java
MINHA_OPCAO_FLOAT("Opacidade", OptionCategoryType.GRAPHICAL, 0.8f, Float.class),
```

Aceita casas decimais. Mesmo comportamento de edição do Integer.

---

### String

```java
MINHA_OPCAO_STR("Prefixo do chat", OptionCategoryType.GENERAL, "[Mod]", String.class),
```

Renderiza como campo de texto livre inline, ao lado do label.

---

### Character (tecla de atalho)

```java
MINHA_TECLA("Abrir menu", OptionCategoryType.KEYBIND, 'G', Character.class),
```

Aceita exatamente um caractere. Útil para keybinds simples.

---

### OptionalData (dropdown)

Para opções com uma lista fechada de escolhas, use `OptionalData` como `defaultValue` e `null` como tipo:

```java
MINHA_OPCAO_DROP("Tema de cores", OptionCategoryType.GRAPHICAL,
        new OptionalData<>(String.class, "Neon", "Claro", "Escuro", "Contraste"), null),
```

> ⚠️ **Importante:** o `defaultValue` deve ser o `OptionalData` inteiro. O índice `0` é o valor padrão selecionado. O campo `type` deve ser `null`.

Ao clicar no campo na UI, abre um **dropdown sobreposto** com todas as opções listadas. O item atualmente selecionado aparece destacado com um `✔`.

---

## Como criar uma categoria

Adicione um novo valor no enum `OptionCategoryType`:

```java
public enum OptionCategoryType {
    GENERAL,
    GRAPHICAL,
    KEYBIND,
    MINHA_CATEGORIA; // <- adicione aqui

    public String getDisplay() {
        // retorna o nome formatado para a sidebar
    }
}
```

Depois associe suas opções a ela normalmente no `OptionType`.

---

## Lendo e escrevendo valores em runtime

Injete o `OptionsService` onde precisar e use os métodos genéricos:

```java
// Leitura
boolean mostrarHud = optionsService.get(OptionType.MINHA_OPCAO_BOOL);
int tamanho        = optionsService.get(OptionType.MINHA_OPCAO_INT);
String prefixo     = optionsService.get(OptionType.MINHA_OPCAO_STR);

// Escrita
optionsService.set(OptionType.MINHA_OPCAO_BOOL, false);
optionsService.set(OptionType.MINHA_OPCAO_INT, 16);

// OptionalData — índice selecionado
int idx    = optionsService.getOptionalIndex(OptionType.MINHA_OPCAO_DROP);
String val = ((OptionalData<String>) optionsService.get(OptionType.MINHA_OPCAO_DROP)).get(idx);

// Forçar a seleção de um índice
optionsService.setOptionalIndex(OptionType.MINHA_OPCAO_DROP, 2);
```

Após modificar valores programaticamente, chame `optionsService.save()` para persistir.

---

## Persistência

O `OptionsService` usa o sistema nativo de `.cfg` do Forge (`Configuration`). O arquivo é salvo automaticamente:

- Ao fechar a tela de configuração (`onGuiClosed`)
- Ao clicar em um toggle Boolean
- Ao selecionar um item no dropdown

Para inicializar o serviço no seu `@Mod` principal:

```java
@EventHandler
public void init(FMLInitializationEvent event) {
    File configFile = new File(event.getModConfigurationDirectory(), "meu_mod.cfg");
    optionsService.init(configFile);
}
```

Cada opção é salva com a chave `nome_da_opcao` na seção correspondente à categoria. `OptionalData` salva apenas o **índice selecionado** com a chave `nome_da_opcao_index`.

---

## Abrindo a UI

```java
mc.displayGuiScreen(new ConfigUI(optionsService));
```

Recomendado associar a uma keybind via `KeyBinding` do Forge, ou chamar a partir de um botão em outra tela.

---

## Requisitos

| Dependência | Versão |
|---|---|
| Minecraft | 1.8.9 |
| Minecraft Forge | 11.15.x |
| Java | 8 |
| Lombok | qualquer estável |

---

<div align="center">
  <sub>Feito para servir de base. Fork, modifique e expanda à vontade.</sub>
</div>